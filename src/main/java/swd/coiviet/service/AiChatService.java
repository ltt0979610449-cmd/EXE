package swd.coiviet.service;

import swd.coiviet.dto.response.AiChatResponse;

/**
 * AI Chat service for tour assistant.
 */
public interface AiChatService {

    /**
     * Process user message and return AI response.
     *
     * @param userId  Current user ID
     * @param content User message content
     * @return AI response
     */
    AiChatResponse sendMessage(Long userId, String content);
}
