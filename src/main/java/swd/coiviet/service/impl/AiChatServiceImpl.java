package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.dto.response.AiChatResponse;
import swd.coiviet.service.AiChatService;
import swd.coiviet.service.ai.LLMClient;
import swd.coiviet.service.ai.AiRagService;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý AI của CoiViet - nền tảng du lịch văn hóa Tây Nguyên.
            Nhiệm vụ: tư vấn tour, văn hóa, nghệ nhân dựa trên CONTEXT.

            QUY TẮC:
            1. Ưu tiên: Trả lời dựa trên CONTEXT khi có thông tin phù hợp.
            2. Ngoài luồng: Khi câu hỏi liên quan du lịch/văn hóa Việt Nam nhưng không có trong CONTEXT, bạn có thể dùng kiến thức chung để trả lời ngắn gọn, thân thiện.
            3. Không liên quan: Nếu câu hỏi hoàn toàn ngoài chủ đề (tin tức thế giới, chính trị...), nhẹ nhàng chuyển hướng về du lịch văn hóa.
            4. Trả lời bằng tiếng Việt, ngắn gọn, thân thiện.
            5. Không bịa thông tin. Chỉ sử dụng dữ liệu trong CONTEXT khi trích dẫn cụ thể (tour, nghệ nhân, giá...).

            VÍ DỤ:
            Q: Tour nào ở Gia Lai? -> A: Dựa trên CONTEXT, liệt kê tour có trong Gia Lai.
            Q: Tây Nguyên có gì đặc biệt? -> A: Nếu không có trong CONTEXT, dùng kiến thức chung: cao nguyên, cà phê, văn hóa bản địa...
            Q: Làm sao đặt tour? -> A: Hướng dẫn: đăng ký tài khoản, chọn tour, chọn lịch, đặt và thanh toán trên CoiViet.

            CONTEXT:
            """;

    private final LLMClient llmClient;
    private final AiRagService aiRagService;

    public AiChatServiceImpl(LLMClient llmClient, AiRagService aiRagService) {
        this.llmClient = llmClient;
        this.aiRagService = aiRagService;
    }

    @Override
    public AiChatResponse sendMessage(Long userId, String content) {
        String context = aiRagService.getContext(content);
        String fullSystemPrompt = SYSTEM_PROMPT + context;

        String reply;
        if (llmClient.isAvailable()) {
            reply = llmClient.chat(fullSystemPrompt, content);
        } else {
            reply = "Tính năng AI chưa được cấu hình. Vui lòng liên hệ quản trị viên. (GEMINI_API_KEY chưa được thiết lập)";
        }

        return AiChatResponse.builder()
                .reply(reply)
                .build();
    }
}
