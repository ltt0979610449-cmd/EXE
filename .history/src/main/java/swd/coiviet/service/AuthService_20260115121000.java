package swd.coiviet.service;

import swd.coiviet.dto.request.LoginRequest;
import swd.coiviet.dto.request.RefreshTokenRequest;
import swd.coiviet.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
}
