package swd.coiviet.service;

import swd.coiviet.dto.response.ChatConversationResponse;
import swd.coiviet.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(Long senderId, Long recipientId, String content);

    List<ChatConversationResponse> getConversations(String username);

    List<ChatMessageResponse> getMessages(String username, Long conversationId);
}
