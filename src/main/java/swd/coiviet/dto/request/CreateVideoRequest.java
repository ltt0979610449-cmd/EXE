package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVideoRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Video URL không được để trống")
    private String videoUrl;

    private Long provinceId;
    private Long cultureItemId;
}
