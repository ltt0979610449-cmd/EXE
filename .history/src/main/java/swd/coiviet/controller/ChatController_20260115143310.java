package swd.coiviet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.ChatConversationResponse;
import swd.coiviet.dto.response.ChatMessageResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> listConversations() {
        String username = getCurrentUsername();
        List<ChatConversationResponse> conversations = chatService.getConversations(username);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long conversationId) {
        String username = getCurrentUsername();
        List<ChatMessageResponse> messages = chatService.getMessages(username, conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        return authentication.getName();
    }
}
