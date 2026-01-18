package swd.coiviet.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.request.GoogleLoginRequest;
import swd.coiviet.dto.request.LoginRequest;
import swd.coiviet.dto.request.RefreshTokenRequest;
import swd.coiviet.dto.response.AuthResponse;
import swd.coiviet.enums.Role;
import swd.coiviet.enums.Status;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.PasswordResetToken;
import swd.coiviet.model.User;
import swd.coiviet.repository.PasswordResetTokenRepository;
import swd.coiviet.service.AuthService;
import swd.coiviet.service.EmailService;
import swd.coiviet.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthServiceImpl(UserService userService,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailService emailService,
                           @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(request.getIdToken());
        } catch (Exception e) {
            logger.error("Google token verification failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.TOKEN_INVALID, "Google token không hợp lệ");
        }

        if (idToken == null) {
            throw new AppException(ErrorCode.TOKEN_INVALID, "Google token không hợp lệ");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        if (payload.getEmail() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không lấy được email từ Google");
        }
        if (payload.getEmailVerified() != null && !payload.getEmailVerified()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Email Google chưa được xác thực");
        }

        User user = userService.findByEmail(payload.getEmail())
                .orElseGet(() -> createUserFromGoogle(payload));

        if (user.getStatus() != Status.ACTIVE) {
            if (user.getStatus() == Status.BANNED) {
                throw new AppException(ErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị khóa");
            }
            throw new AppException(ErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa");
        }

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId().intValue());
        String refreshToken = jwtUtil.refreshToken(accessToken);

        logger.info("User {} logged in with Google successfully", user.getUsername());

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
    @Transactional(readOnly = true)
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

    private User createUserFromGoogle(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
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

        String fullName = payload.get("name") != null ? payload.get("name").toString() : username;
        String avatarUrl = payload.get("picture") != null ? payload.get("picture").toString() : null;

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .build();

        return userService.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Email không tồn tại trong hệ thống"));

        // Check if account is active
        if (user.getStatus() != Status.ACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa hoặc bị khóa");
        }

        // Invalidate any existing unused tokens for this user
        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findByUserIdAndUsedFalse(user.getId());
        for (PasswordResetToken existingToken : existingTokens) {
            existingToken.setUsed(true);
            passwordResetTokenRepository.save(existingToken);
        }

        // Generate OTP (6 digits)
        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10); // OTP expires in 10 minutes

        // Create new reset token with OTP
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(otp)
                .user(user)
                .expiresAt(expiresAt)
                .used(false)
                .attempts(0)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email with OTP
        emailService.sendPasswordResetOtp(user.getEmail(), otp);

        logger.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Email không tồn tại trong hệ thống"));

        PasswordResetToken resetToken = passwordResetTokenRepository.findByUserEmailAndUsedFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID, "OTP không hợp lệ hoặc đã được sử dụng"));

        // Check if OTP is expired
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "OTP đã hết hạn. Vui lòng yêu cầu OTP mới.");
        }

        // Check if OTP is already used
        if (resetToken.getUsed()) {
            throw new AppException(ErrorCode.TOKEN_INVALID, "OTP đã được sử dụng");
        }

        // Check if too many attempts
        if (resetToken.getAttempts() >= 5) {
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            throw new AppException(ErrorCode.TOKEN_INVALID, "Đã nhập sai OTP quá nhiều lần. Vui lòng yêu cầu OTP mới.");
        }

        // Verify OTP
        if (!resetToken.getToken().equals(otp)) {
            resetToken.setAttempts(resetToken.getAttempts() + 1);
            passwordResetTokenRepository.save(resetToken);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, 
                    String.format("OTP không đúng. Bạn còn %d lần thử.", 5 - resetToken.getAttempts()));
        }

        logger.info("OTP verified successfully for user: {}", user.getEmail());
        return true;
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        // Verify OTP first
        verifyOtp(email, otp);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Email không tồn tại trong hệ thống"));

        PasswordResetToken resetToken = passwordResetTokenRepository.findByUserEmailAndUsedFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID, "OTP không hợp lệ"));

        // Update user password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userService.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        logger.info("Password reset successfully for user: {}", user.getEmail());
    }

    private String generateOtp() {
        // Generate 6-digit OTP
        int otp = (int) (Math.random() * 900000) + 100000; // Range: 100000-999999
        return String.valueOf(otp);
    }
}
