package swd.coiviet.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini API client implementation.
 * Uses REST API: https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
 */
@Component
@Primary
public class GeminiLLMClient implements LLMClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiLLMClient.class);
    private static final int MAX_CONTENT_LENGTH = 30000;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (!isAvailable()) {
            throw new IllegalStateException("Gemini API key is not configured. Set GEMINI_API_KEY in .env");
        }

        if (systemPrompt.length() > MAX_CONTENT_LENGTH) {
            systemPrompt = systemPrompt.substring(0, MAX_CONTENT_LENGTH);
        }
        if (userMessage.length() > 2000) {
            userMessage = userMessage.substring(0, 2000);
        }

        String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", userMessage)))
                ),
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024,
                        "topP", 0.95
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        int maxRetries = 3;
        int retryDelayMs = 11000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode candidates = root.path("candidates");
                    if (candidates.isArray() && candidates.size() > 0) {
                        JsonNode content = candidates.get(0).path("content").path("parts");
                        if (content.isArray() && content.size() > 0) {
                            return content.get(0).path("text").asText();
                        }
                    }
                    log.warn("Gemini response unexpected structure: {}", response.getBody());
                    return "Xin lỗi, tôi không thể xử lý câu hỏi này. Vui lòng thử lại.";
                } else {
                    log.error("Gemini API error: {} - {}", response.getStatusCode(), response.getBody());
                    return "Xin lỗi, có lỗi xảy ra khi kết nối tới AI. Vui lòng thử lại sau.";
                }
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt < maxRetries) {
                    log.warn("Gemini rate limit (429), retry {}/{} in {}s", attempt, maxRetries, retryDelayMs / 1000);
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    log.error("Gemini API rate limit after {} retries", maxRetries);
                    return "Đã vượt giới hạn sử dụng AI. Vui lòng thử lại sau vài phút.";
                }
            } catch (Exception e) {
                log.error("Gemini API call failed", e);
                return "Xin lỗi, có lỗi xảy ra khi kết nối tới AI. Vui lòng thử lại sau.";
            }
        }
        return "Xin lỗi, có lỗi xảy ra khi kết nối tới AI. Vui lòng thử lại sau.";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }
}
