package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBlogPostRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String slug;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private Long provinceId;
}
