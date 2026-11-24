package africa.samaki.marketplace.controller;

import africa.samaki.marketplace.model.Role;
import africa.samaki.marketplace.model.User;
import africa.samaki.marketplace.repository.UserRepository;
import africa.samaki.marketplace.service.AdminSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/admin/security")
@PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_DEVELOPER')")
public class AdminSecurityController {

    private final AdminSecurityService securityService;
    private final UserRepository userRepository;

    public AdminSecurityController(AdminSecurityService securityService, UserRepository userRepository) {
        this.securityService = securityService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "password") String tab,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String userQuery,
            Principal principal,
            HttpSession session,
            Model model) {

        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", tab);
        model.addAttribute("currentSessionId", session.getId());

        // 1. Password security info (already on currentUser)

        // 2. 2FA Setup info if session has newly generated secret
        Object pendingSecret = session.getAttribute("NEW_2FA_SECRET");
        Object pendingQr = session.getAttribute("NEW_2FA_QR_URI");
        Object pendingCodes = session.getAttribute("NEW_2FA_BACKUP_CODES");
        model.addAttribute("pending2faSecret", pendingSecret);
        model.addAttribute("pending2faQr", pendingQr);
        model.addAttribute("pending2faBackupCodes", pendingCodes);

        // 3. Admin accounts (Owner only has full management rights, Developer can view)
        model.addAttribute("adminAccounts", securityService.getAdminAndDeveloperAccounts());
        model.addAttribute("isOwner", currentUser.isOwner());

        // 4. Active Sessions
        model.addAttribute("activeSessions", securityService.getAllActiveSessions());

        // 5. User reset management
        model.addAttribute("allUsers", securityService.searchUsers(userQuery));
        model.addAttribute("userSearchQuery", userQuery);

        // 6. Security Audit logs
        model.addAttribute("securityAuditLogs", securityService.getSecurityAuditLogs(q));
        model.addAttribute("auditQuery", q);

        return "admin/security";
    }

    // ==========================================
    // 1. CHANGE CURRENT ADMIN PASSWORD
    // ==========================================

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        String ipAddress = request.getRemoteAddr();

        try {
            securityService.changePassword(currentUser, currentPassword, newPassword, confirmPassword, ipAddress);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully. Your account is secured with the updated credentials.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/security?tab=password";
    }

    // ==========================================
    // 2. TWO-FACTOR AUTHENTICATION SETUP & TOGGLE
    // ==========================================

    @PostMapping("/2fa/setup")
    public String setup2fa(Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        Map<String, Object> setupData = securityService.setup2faSecret(currentUser);

        session.setAttribute("NEW_2FA_SECRET", setupData.get("secret"));
        session.setAttribute("NEW_2FA_QR_URI", setupData.get("qrUri"));
        session.setAttribute("NEW_2FA_BACKUP_CODES", setupData.get("backupCodes"));

        redirectAttributes.addFlashAttribute("infoMessage", "2FA secret key generated. Scan the QR code or enter the secret key into Google Authenticator / Authy, then enter the 6-digit confirmation code below to activate.");
        return "redirect:/admin/security?tab=2fa";
    }

    @PostMapping("/2fa/confirm")
    public String confirm2fa(
            @RequestParam String verificationCode,
            Principal principal,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        String ipAddress = request.getRemoteAddr();

        boolean verified = securityService.confirmAndEnable2fa(currentUser, verificationCode, ipAddress);
        if (verified) {
            session.removeAttribute("NEW_2FA_SECRET");
            session.removeAttribute("NEW_2FA_QR_URI");
            session.removeAttribute("NEW_2FA_BACKUP_CODES");
            redirectAttributes.addFlashAttribute("successMessage", "Two-Factor Authentication (2FA) is now ACTIVATED on your account.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid 6-digit authentication code. Please ensure your device clock is synchronized and try again.");
        }
        return "redirect:/admin/security?tab=2fa";
    }

    @PostMapping("/2fa/disable")
    public String disable2fa(
            @RequestParam String currentPassword,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        String ipAddress = request.getRemoteAddr();

        try {
            securityService.disable2fa(currentUser, currentPassword, ipAddress);
            redirectAttributes.addFlashAttribute("successMessage", "Two-Factor Authentication has been deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/security?tab=2fa";
    }

    // ==========================================
    // 3. ACTIVE SESSIONS REVOCATION
    // ==========================================

    @PostMapping("/sessions/{sessionId}/revoke")
    public String revokeSession(
            @PathVariable String sessionId,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String ipAddress = request.getRemoteAddr();
        securityService.revokeSession(sessionId, principal.getName(), ipAddress);
        redirectAttributes.addFlashAttribute("successMessage", "Selected session access has been immediately revoked.");
        return "redirect:/admin/security?tab=sessions";
    }

    @PostMapping("/sessions/revoke-others")
    public String revokeOtherSessions(
            Principal principal,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        String ipAddress = request.getRemoteAddr();

        securityService.revokeAllOtherUserSessions(currentUser, session.getId(), principal.getName(), ipAddress);
        redirectAttributes.addFlashAttribute("successMessage", "All other active login sessions have been terminated.");
        return "redirect:/admin/security?tab=sessions";
    }

    // ==========================================
    // 4. INITIATE USER PASSWORD RESETS
    // ==========================================

    @PostMapping("/user-resets/initiate")
    public String initiateUserReset(
            @RequestParam Long targetUserId,
            @RequestParam(required = false) String customTempPassword,
            @RequestParam String reason,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String ipAddress = request.getRemoteAddr();

        try {
            String token = securityService.initiateAdminPasswordReset(targetUserId, customTempPassword, reason, principal.getName(), ipAddress);
            String resetUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/reset-password?token=" + token;
            redirectAttributes.addFlashAttribute("successMessage", "Secure password reset initiated. Reset Link: " + resetUrl);
            redirectAttributes.addFlashAttribute("generatedResetUrl", resetUrl);
            redirectAttributes.addFlashAttribute("resetSuccessForUser", targetUserId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/security?tab=user-resets";
    }

    // ==========================================
    // 5. MANAGE ADMIN ACCOUNTS & PERMISSIONS (OWNER EXCLUSIVE)
    // ==========================================

    @PostMapping("/manage-admins/create")
    @PreAuthorize("hasRole('OWNER')")
    public String createAdmin(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam Role role,
            @RequestParam String password,
            @RequestParam(defaultValue = "Kenya") String country,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String ipAddress = request.getRemoteAddr();

        try {
            User admin = securityService.createAdminAccount(fullName, email, phone, role, password, country, principal.getName(), ipAddress);
            redirectAttributes.addFlashAttribute("successMessage", "New administrative account provisioned: " + admin.getFullName() + " (" + admin.getRole() + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/security?tab=admins";
    }

    @PostMapping("/manage-admins/{id}/role")
    @PreAuthorize("hasRole('OWNER')")
    public String updateAdminRole(
            @PathVariable Long id,
            @RequestParam Role role,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String ipAddress = request.getRemoteAddr();

        try {
            securityService.updateAdminRole(id, role, principal.getName(), ipAddress);
            redirectAttributes.addFlashAttribute("successMessage", "Administrator permissions updated to " + role);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/security?tab=admins";
    }

    @PostMapping("/manage-admins/{id}/toggle-status")
    @PreAuthorize("hasRole('OWNER')")
    public String toggleAdminStatus(
            @PathVariable Long id,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String ipAddress = request.getRemoteAddr();

        try {
            boolean active = securityService.toggleAdminStatus(id, principal.getName(), ipAddress);
            redirectAttributes.addFlashAttribute("successMessage", "Account status updated to " + (active ? "ACTIVE" : "INACTIVE"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/security?tab=admins";
    }
}
