package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd.coiviet.enums.PublicationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDetailResponse {
    private Long id;
    private String title;
    private String slug;
    private String heroSubtitle;
    private String featuredImageUrl;
    private String panoramaImageUrl;
    private String content;
    private List<ArtisanDetailResponse.NarrativeBlock> narrativeContent;
    private List<String> images;
    private String location;  // province.name
    private PublicationStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
