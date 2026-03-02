package swd.coiviet.service.ai;

/**
 * Interface for LLM (Large Language Model) clients.
 * Implementations: GeminiLLMClient, OpenAILLMClient, etc.
 */
public interface LLMClient {

    /**
     * Send a chat message and get AI response.
     *
     * @param systemPrompt System instruction to steer model behavior
     * @param userMessage  User's message
     * @return AI response text
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * Check if this client is available (e.g. API key configured).
     */
    boolean isAvailable();
}
