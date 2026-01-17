package swd.coiviet.dto.request;

import lombok.Data;

@Data
public class UpdateBlogPostRequest {
    private String title;
    private String slug;
    private String content;
    private Long provinceId;
    private String blocksJson;
}