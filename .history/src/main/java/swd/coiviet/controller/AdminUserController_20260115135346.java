package swd.coiviet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.request.UpdateUserRoleRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.UserResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.mapper.UserMapper;
import swd.coiviet.model.User;
import swd.coiviet.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public AdminUserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(@PathVariable Long id,
                                                                    @Validated @RequestBody UpdateUserRoleRequest request) {
        User user = userService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        user.setRole(request.getRole());
        User saved = userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(saved), "Cập nhật role thành công"));
    }
}
