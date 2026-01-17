package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserMemoryRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;
    private Long provinceId;
}
