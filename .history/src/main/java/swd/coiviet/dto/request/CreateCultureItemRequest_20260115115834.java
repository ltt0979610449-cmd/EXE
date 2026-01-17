package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import swd.coiviet.enums.CultureCategory;

@Data
public class CreateCultureItemRequest {
    @NotNull(message = "Province ID không được để trống")
    private Long provinceId;

    @NotNull(message = "Category không được để trống")
    private CultureCategory category;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;
    private String videoUrl;
}
