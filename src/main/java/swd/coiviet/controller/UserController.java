package swd.coiviet.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.request.ChangePasswordRequest;
import swd.coiviet.dto.request.CreateUserRequest;
import swd.coiviet.dto.request.UpdateUserRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.UserResponse;
import swd.coiviet.mapper.UserMapper;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.User;
import swd.coiviet.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, UserMapper userMapper, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Validated @RequestBody CreateUserRequest req) {
        // check unique email and username
        userService.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Email already in use");
        });
        userService.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Username already in use");
        });

        User user = userMapper.toEntity(req);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        User saved = userService.save(user);
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(saved)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers() {
        List<UserResponse> users = userService.findAll().stream().map(userMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        User user = userService.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Validated @RequestBody UpdateUserRequest req) {
        User user = userService.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            userService.findByEmail(req.getEmail()).ifPresent(u -> { throw new AppException(ErrorCode.INVALID_REQUEST, "Email already in use"); });
        }
        userMapper.updateFromDto(req, user);
        if (req.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        User saved = userService.save(user);
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(saved)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        userService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Validated @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        User user = userService.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Mật khẩu cũ không đúng");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công"));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ");
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn");
            }
            
            io.jsonwebtoken.Claims claims = jwtUtil.getClaims(token);
            Integer userId = claims.get("userId", Integer.class);
            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không chứa thông tin user");
            }
            return Long.valueOf(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
        }
    }
}
