package swd.coiviet.dto.request;

import lombok.Data;

@Data
public class UpdateBlogPostRequest {
    private String title;
    private String slug;
    private String content;
    private String heroSubtitle;
    private String narrativeContent;
    private Long provinceId;
}