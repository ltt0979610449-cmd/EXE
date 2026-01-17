package swd.coiviet.service;

import swd.coiviet.dto.request.GoogleLoginRequest;
import swd.coiviet.dto.request.LoginRequest;
import swd.coiviet.dto.request.RefreshTokenRequest;
import swd.coiviet.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
    void forgotPassword(String email);
    boolean verifyOtp(String email, String otp);
    void resetPassword(String email, String otp, String newPassword);
}
