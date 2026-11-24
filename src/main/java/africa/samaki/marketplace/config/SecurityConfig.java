package africa.samaki.marketplace.config;

import africa.samaki.marketplace.security.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/signup",
                    "/login",
                    "/login/2fa",
                    "/forgot-password",
                    "/manifest.webmanifest",
                    "/sw.js",
                    "/icons/**",
                    "/reset-password",
                    "/listings",
                    "/listings/{id:[0-9]+}",
                    "/api/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()
                // Owner exclusive business financial control
                .requestMatchers("/admin/financial-control/**").hasRole("OWNER")
                // Owner exclusive administrator provisioning & role permissions
                .requestMatchers("/admin/security/manage-admins/**").hasRole("OWNER")
                // Owner & System Developer security administration
                .requestMatchers("/admin/security/**").hasAnyRole("OWNER", "SYSTEM_DEVELOPER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
