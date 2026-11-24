package africa.samaki.marketplace;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.*;
import africa.samaki.marketplace.service.AdminSecurityService;
import africa.samaki.marketplace.service.TotpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminSecurityServiceTests {

    @Autowired
    private AdminSecurityService securityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private SecurityAuditLogRepository securityAuditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    @Test
    @DisplayName("Should successfully change password with verification and audit logging")
    void testChangePasswordSuccess() {
        User owner = userRepository.findByEmail("owner@samaki.africa").orElseThrow();
        String currentPlain = "password123";
        String newPlain = "Owner#Secure2026";

        securityService.changePassword(owner, currentPlain, newPlain, newPlain, "127.0.0.1");

        User updated = userRepository.findByEmail("owner@samaki.africa").orElseThrow();
        assertTrue(passwordEncoder.matches(newPlain, updated.getPassword()), "New password must match BCrypt hash");
        assertNotNull(updated.getPasswordChangedAt());

        List<SecurityAuditLog> logs = securityAuditLogRepository.searchSecurityLogs("PASSWORD_CHANGE");
        assertFalse(logs.isEmpty(), "Security audit log must be recorded");
        assertEquals("SUCCESS", logs.get(0).getStatus());
    }

    @Test
    @DisplayName("Should reject password change when current password is wrong")
    void testChangePasswordFailure() {
        User owner = userRepository.findByEmail("owner@samaki.africa").orElseThrow();

        assertThrows(IllegalArgumentException.class, () -> {
            securityService.changePassword(owner, "wrongPassword!", "NewPass1234", "NewPass1234", "127.0.0.1");
        });
    }

    @Test
    @DisplayName("Should generate secure reset token, reset password, and invalidate token against reuse")
    void testPasswordRecoveryFlow() {
        User targetUser = userRepository.findByEmail("developer@samaki.africa")
                .or(() -> userRepository.findByEmail("owner@samaki.africa"))
                .orElseThrow();

        // 1. Create reset token
        Optional<PasswordResetToken> tokenOpt = securityService.createPasswordResetToken(targetUser.getEmail(), "SELF", "127.0.0.1");
        assertTrue(tokenOpt.isPresent(), "Reset token should be generated");
        PasswordResetToken token = tokenOpt.get();
        assertTrue(token.isValid());

        // 2. Complete reset with token
        String brandNewPass = "FisherSecret2026!";
        securityService.resetPasswordWithToken(token.getToken(), brandNewPass, brandNewPass, "127.0.0.1");

        // 3. Verify user has new password
        User updated = userRepository.findByEmail(targetUser.getEmail()).orElseThrow();
        assertTrue(passwordEncoder.matches(brandNewPass, updated.getPassword()));

        // 4. Invalidate token against reuse
        Optional<PasswordResetToken> consumed = resetTokenRepository.findByToken(token.getToken());
        assertTrue(consumed.isPresent());
        assertTrue(consumed.get().isUsed(), "Token must be marked as used");
        assertFalse(consumed.get().isValid(), "Used token must not be valid");

        // Subsequent attempt with consumed token must fail
        assertThrows(IllegalArgumentException.class, () -> {
            securityService.resetPasswordWithToken(token.getToken(), "AnotherPass123", "AnotherPass123", "127.0.0.1");
        });
    }

    @Test
    @DisplayName("Should configure 2FA secret, backup recovery codes, and verify backup code")
    void testTwoFactorAuthenticationFlow() {
        User developer = userRepository.findByEmail("developer@samaki.africa").orElseThrow();

        var setupData = securityService.setup2faSecret(developer);
        String secret = (String) setupData.get("secret");
        @SuppressWarnings("unchecked")
        List<String> backupCodes = (List<String>) setupData.get("backupCodes");

        assertNotNull(secret);
        assertEquals(5, backupCodes.size());

        // Verify with backup code
        String testBackup = backupCodes.get(0);
        developer.setTwoFactorEnabled(true);
        userRepository.save(developer);

        boolean verified = securityService.verify2faCode(developer, testBackup);
        assertTrue(verified, "Backup recovery code should verify access");

        // Verify that the consumed backup code is removed
        User reloaded = userRepository.findByEmail("developer@samaki.africa").orElseThrow();
        assertFalse(reloaded.getTwoFactorBackupCodes().contains(testBackup), "Consumed backup code must be removed");

        // Disable 2FA with current password
        securityService.disable2fa(reloaded, "password123", "127.0.0.1");
        User disabledUser = userRepository.findByEmail("developer@samaki.africa").orElseThrow();
        assertFalse(disabledUser.isTwoFactorEnabled());
    }

    @Test
    @DisplayName("Should enforce Owner ultimate authority over admin accounts and block developer privilege escalation")
    void testOwnerAuthorityAndDeveloperRestrictions() {
        // Owner can provision a new administrator
        User createdAdmin = securityService.createAdminAccount("Test Admin", "test.admin@samaki.africa",
                "+254799001122", Role.ADMIN, "Admin#Pass2026", "Kenya", "owner@samaki.africa", "127.0.0.1");
        assertNotNull(createdAdmin.getId());
        assertEquals(Role.ADMIN, createdAdmin.getRole());

        // Owner can toggle admin active status
        boolean active = securityService.toggleAdminStatus(createdAdmin.getId(), "owner@samaki.africa", "127.0.0.1");
        assertFalse(active, "Account should be deactivated");

        // System Developer cannot create an admin account (throws SecurityException)
        assertThrows(SecurityException.class, () -> {
            securityService.createAdminAccount("Rogue Admin", "rogue@samaki.africa", "+254799009988",
                    Role.ADMIN, "Rogue#Pass2026", "Kenya", "developer@samaki.africa", "127.0.0.1");
        });

        // System Developer cannot reset Platform Owner's password
        User owner = userRepository.findByEmail("owner@samaki.africa").orElseThrow();
        assertThrows(SecurityException.class, () -> {
            securityService.initiateAdminPasswordReset(owner.getId(), "NewOwnerPass123", "Unauthorized reset attempt",
                    "developer@samaki.africa", "127.0.0.1");
        });
    }

    @Test
    @DisplayName("Should track and revoke active login sessions")
    void testSessionManagementAndRevocation() {
        User owner = userRepository.findByEmail("owner@samaki.africa").orElseThrow();
        String sessionId = "test-session-xyz-12345";

        securityService.recordLoginSession(sessionId, owner, "192.168.1.50", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0");

        List<UserLoginSession> activeSessions = securityService.getAllActiveSessions();
        assertTrue(activeSessions.stream().anyMatch(s -> s.getSessionId().equals(sessionId)));

        // Revoke session
        securityService.revokeSession(sessionId, "owner@samaki.africa", "127.0.0.1");

        List<UserLoginSession> updatedActive = securityService.getAllActiveSessions();
        assertFalse(updatedActive.stream().anyMatch(s -> s.getSessionId().equals(sessionId)), "Revoked session must not be active");
    }
}
