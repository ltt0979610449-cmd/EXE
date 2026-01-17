package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.NotificationResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Notification;
import swd.coiviet.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    public NotificationController(NotificationService notificationService, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo của user", description = "Lấy tất cả thông báo của user hiện tại, sắp xếp theo thời gian mới nhất")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam(value = "unreadOnly", defaultValue = "false") Boolean unreadOnly,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        
        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationService.findByUserIdAndIsReadFalse(userId);
        } else {
            notifications = notificationService.findByUserIdOrderByCreatedAtDesc(userId);
        }
        
        List<NotificationResponse> responses = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Đếm số thông báo chưa đọc", description = "Lấy số lượng thông báo chưa đọc của user")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long count = notificationService.countUnreadByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Đánh dấu thông báo đã đọc", description = "Đánh dấu một thông báo là đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Thông báo không tồn tại"));
        
        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền truy cập thông báo này");
        }
        
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu đã đọc"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc", description = "Đánh dấu tất cả thông báo của user là đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu tất cả đã đọc"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thông báo", description = "Xóa một thông báo")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Thông báo không tồn tại"));
        
        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xóa thông báo này");
        }
        
        notificationService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa thông báo thành công"));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
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
