package africa.samaki.marketplace.security;

import africa.samaki.marketplace.model.User;
import africa.samaki.marketplace.repository.UserRepository;
import africa.samaki.marketplace.service.AdminSecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AdminSecurityService securityService;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository, @Lazy AdminSecurityService securityService) {
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            HttpSession session = request.getSession(true);
            String sessionId = session.getId();
            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            // Record live session in persistent session registry
            securityService.recordLoginSession(sessionId, user, ipAddress, userAgent);

            // Check if Two-Factor Authentication is enabled on this account
            if (user.isTwoFactorEnabled()) {
                session.setAttribute("2FA_PENDING_USER_ID", user.getId());
                session.setAttribute("2FA_VERIFIED", false);
                response.sendRedirect("/login/2fa");
                return;
            } else {
                session.setAttribute("2FA_VERIFIED", true);
            }
        }

        response.sendRedirect("/dashboard");
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
