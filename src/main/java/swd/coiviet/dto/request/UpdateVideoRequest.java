package swd.coiviet.dto.request;

import lombok.Data;

@Data
public class UpdateVideoRequest {
    private String title;
    private String videoUrl;
    private Long provinceId;
    private Long cultureItemId;
}