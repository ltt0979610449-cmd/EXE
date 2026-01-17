package swd.coiviet.dto.request;

import lombok.Data;
import swd.coiviet.enums.CultureCategory;

@Data
public class UpdateCultureItemRequest {
    private Long provinceId;
    private CultureCategory category;
    private String title;
    private String description;
    private String videoUrl;
}