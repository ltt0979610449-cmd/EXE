package swd.coiviet.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import swd.coiviet.dto.request.ChatSendRequest;
import swd.coiviet.dto.response.ChatMessageResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.service.ChatService;

import java.security.Principal;

@Controller
public class ChatSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatSendRequest request, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        Long senderId;
        try {
            senderId = Long.parseLong(principal.getName());
        } catch (NumberFormatException ex) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid user principal");
        }

        ChatMessageResponse response = chatService.sendMessage(
                senderId, request.getRecipientId(), request.getContent());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.getRecipientId()), "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.getSenderId()), "/queue/messages", response);
    }
}
