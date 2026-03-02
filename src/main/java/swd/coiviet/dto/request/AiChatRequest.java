package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 500, message = "Tin nhắn tối đa 500 ký tự")
    private String content;
}
