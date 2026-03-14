package swd.coiviet.controller.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.request.AiChatRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.AiChatResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.service.AiChatService;

@RestController
@RequestMapping("/api/ai-chat")
@Tag(name = "AI Chat", description = "Chat với AI tư vấn tour, văn hóa")
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {

    private final AiChatService aiChatService;
    private final JwtUtil jwtUtil;

    public AiChatController(AiChatService aiChatService, JwtUtil jwtUtil) {
        this.aiChatService = aiChatService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/messages")
    @Operation(summary = "Gửi tin nhắn tới AI trợ lý", description = "Gửi câu hỏi và nhận phản hồi từ AI tư vấn tour, văn hóa, nghệ nhân")
    public ResponseEntity<ApiResponse<AiChatResponse>> sendMessage(
            @Valid @RequestBody AiChatRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        AiChatResponse response = aiChatService.sendMessage(userId, request.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
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
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không chứa thông tin user");
            }
            return userId.longValue();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
        }
    }
}
