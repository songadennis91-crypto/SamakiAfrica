package africa.samaki.marketplace.service;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminSecurityService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserLoginSessionRepository sessionRepository;
    private final SecurityAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;

    public AdminSecurityService(UserRepository userRepository,
                                PasswordResetTokenRepository resetTokenRepository,
                                UserLoginSessionRepository sessionRepository,
                                SecurityAuditLogRepository auditLogRepository,
                                PasswordEncoder passwordEncoder,
                                TotpService totpService) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.sessionRepository = sessionRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.totpService = totpService;
    }

    // ==========================================
    // 1. PASSWORD MANAGEMENT & CHANGE
    // ==========================================

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword, String confirmPassword, String ipAddress) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logSecurityEvent("PASSWORD_CHANGE", user.getEmail(), user.getEmail(), ipAddress, "FAILED", "Current password verification failed.");
            throw new IllegalArgumentException("The current password entered is incorrect.");
        }

        validatePasswordStrength(newPassword, confirmPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        logSecurityEvent("PASSWORD_CHANGE", user.getEmail(), user.getEmail(), ipAddress, "SUCCESS", "User successfully changed account password.");
    }

    private void validatePasswordStrength(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and password confirmation do not match.");
        }
        boolean hasLetter = newPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = newPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("Password must contain a mix of letters and numerical digits.");
        }
    }

    // ==========================================
    // 2. RECOVER FORGOTTEN PASSWORDS (SELF-SERVICE)
    // ==========================================

    @Transactional
    public Optional<PasswordResetToken> createPasswordResetToken(String emailOrPhone, String createdBy, String ipAddress) {
        if (emailOrPhone == null || emailOrPhone.isBlank()) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findByEmail(emailOrPhone.trim())
                .or(() -> userRepository.findByPhone(emailOrPhone.trim()));

        if (userOpt.isEmpty()) {
            logSecurityEvent("PASSWORD_RESET_REQUEST", emailOrPhone, createdBy != null ? createdBy : "ANONYMOUS",
                    ipAddress, "FAILED", "Reset request for unregistered identity: " + emailOrPhone);
            return Optional.empty();
        }

        User user = userOpt.get();
        String tokenStr = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        PasswordResetToken token = new PasswordResetToken(tokenStr, user, expiry, createdBy);
        resetTokenRepository.save(token);

        logSecurityEvent("PASSWORD_RESET_REQUEST", user.getEmail(), createdBy != null ? createdBy : "ANONYMOUS",
                ipAddress, "SUCCESS", "Password reset token generated. Valid until: " + expiry);

        return Optional.of(token);
    }

    public Optional<PasswordResetToken> validatePasswordResetToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return resetTokenRepository.findByToken(token.trim())
                .filter(PasswordResetToken::isValid);
    }

    @Transactional
    public void resetPasswordWithToken(String tokenStr, String newPassword, String confirmPassword, String ipAddress) {
        PasswordResetToken token = validatePasswordResetToken(tokenStr)
                .orElseThrow(() -> new IllegalArgumentException("The password recovery token is invalid or has expired."));

        validatePasswordStrength(newPassword, confirmPassword);

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsed(true);
        resetTokenRepository.save(token);

        // Security requirement: Revoke existing sessions upon password recovery
        revokeAllUserSessionsInternal(user.getId(), "SYSTEM_PASSWORD_RESET");

        logSecurityEvent("PASSWORD_RESET_SUCCESS", user.getEmail(), user.getEmail(), ipAddress, "SUCCESS",
                "Password successfully recovered using single-use recovery token.");
    }

    // ==========================================
    // 3. TWO-FACTOR AUTHENTICATION (2FA / MFA)
    // ==========================================

    @Transactional
    public Map<String, Object> setup2faSecret(User user) {
        String secret = totpService.generateSecret();
        List<String> backupCodes = totpService.generateBackupCodes(5);

        user.setTwoFactorSecret(secret);
        user.setTwoFactorBackupCodes(String.join(",", backupCodes));
        userRepository.save(user);

        String qrUri = totpService.getTotpUri(user.getEmail(), secret);

        Map<String, Object> result = new HashMap<>();
        result.put("secret", secret);
        result.put("backupCodes", backupCodes);
        result.put("qrUri", qrUri);
        return result;
    }

    @Transactional
    public boolean confirmAndEnable2fa(User user, String verificationCode, String ipAddress) {
        if (user.getTwoFactorSecret() == null) {
            throw new IllegalStateException("2FA secret has not been initialized. Please start setup again.");
        }

        boolean valid = totpService.verifyCode(user.getTwoFactorSecret(), verificationCode);
        if (!valid) {
            logSecurityEvent("2FA_CONFIRMATION", user.getEmail(), user.getEmail(), ipAddress, "FAILED",
                    "Invalid 6-digit verification code entered during 2FA enablement.");
            return false;
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        logSecurityEvent("2FA_ENABLED", user.getEmail(), user.getEmail(), ipAddress, "SUCCESS",
                "Two-Factor Authentication (TOTP) successfully activated on account.");
        return true;
    }

    @Transactional
    public void disable2fa(User user, String currentPassword, String ipAddress) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logSecurityEvent("2FA_DISABLED", user.getEmail(), user.getEmail(), ipAddress, "FAILED",
                    "Password verification failed during 2FA deactivation request.");
            throw new IllegalArgumentException("Current password verification failed. Cannot disable 2FA.");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorBackupCodes(null);
        userRepository.save(user);

        logSecurityEvent("2FA_DISABLED", user.getEmail(), user.getEmail(), ipAddress, "SUCCESS",
                "Two-Factor Authentication deactivated on account.");
    }

    public boolean verify2faCode(User user, String code) {
        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            return true; // Not enabled
        }

        if (code == null || code.isBlank()) {
            return false;
        }

        String cleanCode = code.trim();

        // 1. Try TOTP code
        if (totpService.verifyCode(user.getTwoFactorSecret(), cleanCode)) {
            return true;
        }

        // 2. Try one-time backup recovery codes
        if (user.getTwoFactorBackupCodes() != null) {
            List<String> codes = new ArrayList<>(Arrays.asList(user.getTwoFactorBackupCodes().split(",")));
            if (codes.remove(cleanCode)) {
                user.setTwoFactorBackupCodes(String.join(",", codes));
                userRepository.save(user);
                logSecurityEvent("2FA_BACKUP_CODE_USED", user.getEmail(), user.getEmail(), "127.0.0.1", "SUCCESS",
                        "One-time backup recovery code consumed. " + codes.size() + " codes remaining.");
                return true;
            }
        }

        return false;
    }

    // ==========================================
    // 4. ACTIVE SESSIONS & ACCESS REVOCATION
    // ==========================================

    @Transactional
    public void recordLoginSession(String sessionId, User user, String ipAddress, String userAgent) {
        String deviceType = parseDeviceType(userAgent);
        UserLoginSession session = sessionRepository.findBySessionId(sessionId)
                .orElse(new UserLoginSession(sessionId, user, ipAddress, userAgent, deviceType));

        session.setUser(user);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceType(deviceType);
        session.setLastAccessTime(LocalDateTime.now());
        session.setActive(true);
        sessionRepository.save(session);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void updateSessionAccess(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(s -> {
            s.setLastAccessTime(LocalDateTime.now());
            sessionRepository.save(s);
        });
    }

    public List<UserLoginSession> getAllActiveSessions() {
        return sessionRepository.findAllByActiveTrueOrderByLastAccessTimeDesc();
    }

    public List<UserLoginSession> getSessionsForUser(User user) {
        return sessionRepository.findByUserOrderByLoginTimeDesc(user);
    }

    @Transactional
    public void revokeSession(String sessionId, String revokedBy, String ipAddress) {
        sessionRepository.findBySessionId(sessionId).ifPresent(s -> {
            s.revoke(revokedBy);
            sessionRepository.save(s);
            logSecurityEvent("SESSION_REVOKED", s.getUser().getEmail(), revokedBy, ipAddress, "SUCCESS",
                    "Session " + sessionId.substring(0, Math.min(8, sessionId.length())) + "... revoked by " + revokedBy);
        });
    }

    @Transactional
    public void revokeAllOtherUserSessions(User user, String currentSessionId, String revokedBy, String ipAddress) {
        List<UserLoginSession> activeSessions = sessionRepository.findByUserAndActiveTrueOrderByLastAccessTimeDesc(user);
        for (UserLoginSession s : activeSessions) {
            if (!s.getSessionId().equals(currentSessionId)) {
                s.revoke(revokedBy);
                sessionRepository.save(s);
            }
        }
        logSecurityEvent("SESSION_REVOKED_ALL", user.getEmail(), revokedBy, ipAddress, "SUCCESS",
                "Revoked all other active sessions for user: " + user.getEmail());
    }

    private void revokeAllUserSessionsInternal(Long userId, String revokedBy) {
        userRepository.findById(userId).ifPresent(user -> {
            List<UserLoginSession> activeSessions = sessionRepository.findByUserAndActiveTrueOrderByLastAccessTimeDesc(user);
            for (UserLoginSession s : activeSessions) {
                s.revoke(revokedBy);
                sessionRepository.save(s);
            }
        });
    }

    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "Desktop";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        }
        return "Desktop";
    }

    // ==========================================
    // 5. INITIATE SECURE PASSWORDS RESETS FOR USERS
    // ==========================================

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAllByOrderByFullNameAsc();
        }
        return userRepository.searchUsers(query.trim());
    }

    @Transactional
    public String initiateAdminPasswordReset(Long targetUserId, String customTempPassword, String reason, String adminEmail, String ipAddress) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + targetUserId));

        // Security check: System Developers cannot reset Owner passwords
        if (targetUser.isOwner()) {
            User admin = userRepository.findByEmail(adminEmail).orElseThrow();
            if (!admin.isOwner()) {
                logSecurityEvent("ADMIN_RESET_DENIED", targetUser.getEmail(), adminEmail, ipAddress, "FAILED",
                        "Unauthorized attempt by non-owner to reset Owner password. Blocked by security policy.");
                throw new SecurityException("Permission denied: Only the Platform Owner can perform credentials maintenance on an Owner account.");
            }
        }

        String resetTokenStr = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);
        PasswordResetToken token = new PasswordResetToken(resetTokenStr, targetUser, expiry, "ADMIN: " + adminEmail);
        resetTokenRepository.save(token);

        if (customTempPassword != null && !customTempPassword.isBlank()) {
            targetUser.setPassword(passwordEncoder.encode(customTempPassword.trim()));
            targetUser.setPasswordChangedAt(LocalDateTime.now());
            userRepository.save(targetUser);
        }

        // Revoke all existing sessions for that user for immediate containment
        revokeAllUserSessionsInternal(targetUserId, "ADMIN_PASSWORD_RESET: " + adminEmail);

        logSecurityEvent("ADMIN_PASSWORD_RESET_INITIATED", targetUser.getEmail(), adminEmail, ipAddress, "SUCCESS",
                "Admin initiated password reset for user. Reason: " + reason + " | Temporary Token Generated.");

        return resetTokenStr;
    }

    // ==========================================
    // 6. MANAGE ADMINISTRATOR ACCOUNTS & PERMISSIONS (OWNER EXCLUSIVE)
    // ==========================================

    public List<User> getAdminAndDeveloperAccounts() {
        return userRepository.findByRoleInOrderByFullNameAsc(List.of(Role.OWNER, Role.SYSTEM_DEVELOPER, Role.ADMIN));
    }

    @Transactional
    public User createAdminAccount(String fullName, String email, String phone, Role role, String rawPassword, String country, String creatorEmail, String ipAddress) {
        User creator = userRepository.findByEmail(creatorEmail).orElseThrow();
        if (!creator.isOwner()) {
            logSecurityEvent("ADMIN_CREATION_DENIED", email, creatorEmail, ipAddress, "FAILED",
                    "Unauthorized non-owner tried to create administrative account.");
            throw new SecurityException("Only the Platform Owner retains ultimate authority to provision administrative accounts.");
        }

        if (userRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("Email address is already in use by another account.");
        }
        if (userRepository.existsByPhone(phone.trim())) {
            throw new IllegalArgumentException("Phone number is already in use by another account.");
        }

        validatePasswordStrength(rawPassword, rawPassword);

        User newAdmin = new User();
        newAdmin.setFullName(fullName.trim());
        newAdmin.setEmail(email.trim());
        newAdmin.setPhone(phone.trim());
        newAdmin.setRole(role);
        newAdmin.setPassword(passwordEncoder.encode(rawPassword.trim()));
        newAdmin.setCountry(country != null && !country.isBlank() ? country.trim() : "Kenya");
        newAdmin.setActive(true);
        newAdmin.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(newAdmin);

        logSecurityEvent("ADMIN_ACCOUNT_CREATED", saved.getEmail(), creatorEmail, ipAddress, "SUCCESS",
                "Provisioned new administrative account: " + saved.getFullName() + " with role " + saved.getRole());

        return saved;
    }

    @Transactional
    public void updateAdminRole(Long adminId, Role newRole, String creatorEmail, String ipAddress) {
        User creator = userRepository.findByEmail(creatorEmail).orElseThrow();
        if (!creator.isOwner()) {
            throw new SecurityException("Only the Platform Owner retains authority to modify administrative permissions.");
        }

        User target = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found: " + adminId));

        if (target.isOwner() && newRole != Role.OWNER) {
            throw new IllegalArgumentException("The Platform Owner role cannot be demoted or revoked.");
        }

        Role prevRole = target.getRole();
        target.setRole(newRole);
        userRepository.save(target);

        logSecurityEvent("ADMIN_ROLE_UPDATED", target.getEmail(), creatorEmail, ipAddress, "SUCCESS",
                "Role changed from " + prevRole + " to " + newRole);
    }

    @Transactional
    public boolean toggleAdminStatus(Long adminId, String creatorEmail, String ipAddress) {
        User creator = userRepository.findByEmail(creatorEmail).orElseThrow();
        if (!creator.isOwner()) {
            throw new SecurityException("Only the Platform Owner retains authority to toggle administrative account status.");
        }

        User target = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found: " + adminId));

        if (target.isOwner()) {
            throw new IllegalArgumentException("The Platform Owner account cannot be deactivated.");
        }

        boolean newState = !target.isActive();
        target.setActive(newState);
        userRepository.save(target);

        if (!newState) {
            revokeAllUserSessionsInternal(target.getId(), "ADMIN_ACCOUNT_DEACTIVATED: " + creatorEmail);
        }

        logSecurityEvent("ADMIN_STATUS_TOGGLED", target.getEmail(), creatorEmail, ipAddress, "SUCCESS",
                "Account status toggled to: " + (newState ? "ACTIVE" : "INACTIVE"));

        return newState;
    }

    // ==========================================
    // 7. SECURITY AUDIT LOGGING
    // ==========================================

    public List<SecurityAuditLog> getSecurityAuditLogs(String query) {
        if (query == null || query.isBlank()) {
            return auditLogRepository.findTop100ByOrderByTimestampDesc();
        }
        return auditLogRepository.searchSecurityLogs(query.trim());
    }

    @Transactional
    public void logSecurityEvent(String eventType, String targetEmail, String performedBy,
                                 String ipAddress, String status, String details) {
        SecurityAuditLog log = new SecurityAuditLog(eventType, targetEmail, performedBy, ipAddress, status, details);
        auditLogRepository.save(log);
    }
}
