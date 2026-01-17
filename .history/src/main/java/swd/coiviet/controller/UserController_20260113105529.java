package swd.coiviet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

    public UserController(UserService userService, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
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

    // mapping handled by UserMapper
}
