package swd.coiviet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.request.CreateUserRequest;
import swd.coiviet.dto.request.UpdateUserRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.UserResponse;
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

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .dateOfBirth(req.getDateOfBirth())
                .build();

        User saved = userService.save(user);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(saved)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers() {
        List<UserResponse> users = userService.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        User user = userService.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Validated @RequestBody UpdateUserRequest req) {
        User user = userService.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            userService.findByEmail(req.getEmail()).ifPresent(u -> { throw new AppException(ErrorCode.INVALID_REQUEST, "Email already in use"); });
            user.setEmail(req.getEmail());
        }

        if (req.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());

        User saved = userService.save(user);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(saved)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        userService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    private UserResponse mapToResponse(User u) {
        if (u == null) return null;
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setFullName(u.getFullName());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setDateOfBirth(u.getDateOfBirth());
        r.setGender(u.getGender());
        r.setRole(u.getRole());
        r.setStatus(u.getStatus());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
