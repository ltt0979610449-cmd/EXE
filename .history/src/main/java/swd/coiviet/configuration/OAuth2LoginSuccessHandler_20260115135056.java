package swd.coiviet.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import swd.coiviet.enums.Role;
import swd.coiviet.enums.Status;
import swd.coiviet.model.User;
import swd.coiviet.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String redirectSuccessUrl;

    public OAuth2LoginSuccessHandler(UserService userService,
                                     PasswordEncoder passwordEncoder,
                                     JwtUtil jwtUtil,
                                     @Value("${app.oauth2.redirect-success}") String redirectSuccessUrl) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redirectSuccessUrl = redirectSuccessUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String email = getStringAttribute(oauthUser, "email");

        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Không lấy được email từ Google");
            return;
        }

        User user = userService.findByEmail(email)
                .orElseGet(() -> createUserFromOAuth(oauthUser, email));

        if (user.getStatus() != Status.ACTIVE) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản đã bị vô hiệu hóa");
            return;
        }

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId().intValue());
        String refreshToken = jwtUtil.refreshToken(accessToken);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectSuccessUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("tokenType", "Bearer")
                .queryParam("expiresIn", jwtUtil.getExpiration())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }

    private User createUserFromOAuth(OAuth2User oauthUser, String email) {
        String baseUsername = email.split("@", 2)[0].replaceAll("[^a-zA-Z0-9._-]", "");
        if (baseUsername.isBlank()) {
            baseUsername = "user";
        }

        String username = baseUsername;
        int suffix = 1;
        while (userService.findByUsername(username).isPresent()) {
            username = baseUsername + suffix;
            suffix++;
        }

        String fullName = getStringAttribute(oauthUser, "name");
        String avatarUrl = getStringAttribute(oauthUser, "picture");

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .fullName(fullName != null ? fullName : username)
                .avatarUrl(avatarUrl)
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .build();

        return userService.save(user);
    }

    private String getStringAttribute(OAuth2User oauthUser, String key) {
        Object value = oauthUser.getAttributes().get(key);
        return value != null ? value.toString() : null;
    }
}
