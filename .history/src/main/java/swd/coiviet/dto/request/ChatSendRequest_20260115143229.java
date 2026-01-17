package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatSendRequest {
    @NotNull(message = "Recipient ID không được để trống")
    private Long recipientId;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
}
