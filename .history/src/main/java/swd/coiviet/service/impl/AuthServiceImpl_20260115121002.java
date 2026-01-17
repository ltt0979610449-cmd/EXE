package swd.coiviet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.request.LoginRequest;
import swd.coiviet.dto.request.RefreshTokenRequest;
import swd.coiviet.dto.response.AuthResponse;
import swd.coiviet.enums.Status;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.User;
import swd.coiviet.service.AuthService;
import swd.coiviet.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by username
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS, "Tên đăng nhập hoặc mật khẩu không đúng"));

        // Check if account is active
        if (user.getStatus() != Status.ACTIVE) {
            if (user.getStatus() == Status.BANNED) {
                throw new AppException(ErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị khóa");
            }
            throw new AppException(ErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId().intValue());
        String refreshToken = jwtUtil.refreshToken(accessToken);

        logger.info("User {} logged in successfully", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtUtil.getExpiration())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            if (!jwtUtil.canRefreshToken(request.getRefreshToken())) {
                throw new AppException(ErrorCode.TOKEN_EXPIRED, "Refresh token đã hết hạn");
            }

            String newAccessToken = jwtUtil.refreshToken(request.getRefreshToken());
            
            // Get user info from token
            String username = jwtUtil.getUsernameFromToken(newAccessToken);
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

            // Check if account is still active
            if (user.getStatus() != Status.ACTIVE) {
                throw new AppException(ErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa");
            }

            String newRefreshToken = jwtUtil.refreshToken(newAccessToken);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .expiresIn(jwtUtil.getExpiration())
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.TOKEN_INVALID, "Refresh token không hợp lệ");
        }
    }

    @Override
    public void logout(String token) {
        // In a stateless JWT system, logout is typically handled client-side
        // by removing the token. However, we can add token blacklisting here if needed.
        logger.info("User logged out");
    }
}
