package africa.samaki.marketplace.controller;

import africa.samaki.marketplace.model.PasswordResetToken;
import africa.samaki.marketplace.model.User;
import africa.samaki.marketplace.repository.UserRepository;
import africa.samaki.marketplace.service.AdminSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordRecoveryController {

    private final AdminSecurityService securityService;
    private final UserRepository userRepository;

    public PasswordRecoveryController(AdminSecurityService securityService, UserRepository userRepository) {
        this.securityService = securityService;
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. FORGOT PASSWORD REQUEST
    // ==========================================

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam String emailOrPhone,
            HttpServletRequest request,
            Model model) {
        String ipAddress = request.getRemoteAddr();
        Optional<PasswordResetToken> tokenOpt = securityService.createPasswordResetToken(emailOrPhone, "SELF", ipAddress);

        model.addAttribute("submitted", true);
        model.addAttribute("identity", emailOrPhone);

        // For local development and demonstration environment, surface the secure reset URL directly
        tokenOpt.ifPresent(token -> {
            String resetUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/reset-password?token=" + token.getToken();
            model.addAttribute("directResetUrl", resetUrl);
            model.addAttribute("tokenFound", true);
        });

        return "forgot-password";
    }

    // ==========================================
    // 2. RESET PASSWORD WITH SECURE TOKEN
    // ==========================================

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("error", "No password reset token provided. Please initiate a new recovery request.");
            return "reset-password";
        }

        Optional<PasswordResetToken> tokenOpt = securityService.validatePasswordResetToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("error", "The password reset token is invalid, already consumed, or has expired.");
            return "reset-password";
        }

        PasswordResetToken validToken = tokenOpt.get();
        model.addAttribute("token", token);
        model.addAttribute("userEmail", validToken.getUser().getEmail());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpServletRequest request,
            Model model) {
        String ipAddress = request.getRemoteAddr();

        try {
            securityService.resetPasswordWithToken(token, newPassword, confirmPassword, ipAddress);
            return "redirect:/login?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }

    // ==========================================
    // 3. TWO-FACTOR AUTHENTICATION LOGIN VERIFICATION
    // ==========================================

    @GetMapping("/login/2fa")
    public String show2faVerification(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("2FA_PENDING_USER_ID");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("userEmail", user.getEmail());
        return "login-2fa";
    }

    @PostMapping("/login/2fa")
    public String verify2faCode(
            @RequestParam String code,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("2FA_PENDING_USER_ID");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        boolean verified = securityService.verify2faCode(user, code);
        if (verified) {
            session.removeAttribute("2FA_PENDING_USER_ID");
            session.setAttribute("2FA_VERIFIED", true);
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid 6-digit authentication code or backup recovery code. Please check and try again.");
            return "redirect:/login/2fa";
        }
    }
}
