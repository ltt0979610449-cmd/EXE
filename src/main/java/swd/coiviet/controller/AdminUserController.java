package swd.coiviet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.request.UpdateUserRoleRequest;
import swd.coiviet.dto.request.UpdateUserStatusRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.UserResponse;
import swd.coiviet.enums.Role;
import swd.coiviet.enums.Status;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.mapper.UserMapper;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.User;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.UserService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final ArtisanService artisanService;

    public AdminUserController(UserService userService, UserMapper userMapper, ArtisanService artisanService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.artisanService = artisanService;
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(@PathVariable Long id,
                                                                    @Validated @RequestBody UpdateUserRoleRequest request) {
        User user = userService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        Role newRole = request.getRole();

        user.setRole(newRole);
        User saved = userService.save(user);

        // Nếu role mới là ARTISAN và chưa có bản ghi Artisan, tạo mới
        if (newRole == Role.ARTISAN) {
            artisanService.findByUserId(user.getId()).ifPresentOrElse(
                    artisan -> {
                        // Đã có bản ghi Artisan, không cần làm gì
                    },
                    () -> {
                        // Chưa có bản ghi Artisan, tạo mới
                        String fullName = user.getFullName() != null && !user.getFullName().isEmpty() 
                                ? user.getFullName() 
                                : user.getUsername();
                        
                        Artisan artisan = Artisan.builder()
                                .user(user)
                                .fullName(fullName)
                                .specialization("Chưa cập nhật") // Giá trị mặc định, có thể cập nhật sau
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .build();
                        
                        artisanService.save(artisan);
                    }
            );
        }

        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(saved), "Cập nhật role thành công"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(@PathVariable Long id,
                                                                      @Validated @RequestBody UpdateUserStatusRequest request) {
        User user = userService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        Status newStatus = request.getStatus();
        user.setStatus(newStatus);
        User saved = userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(saved), 
                "Cập nhật trạng thái thành công: " + getStatusMessage(newStatus)));
    }

    private String getStatusMessage(Status status) {
        return switch (status) {
            case ACTIVE -> "Tài khoản đã được kích hoạt";
            case INACTIVE -> "Tài khoản đã bị vô hiệu hóa";
            case BANNED -> "Tài khoản đã bị khóa";
        };
    }
}
