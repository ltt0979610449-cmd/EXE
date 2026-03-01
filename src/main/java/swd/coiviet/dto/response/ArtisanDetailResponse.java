package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanDetailResponse {
    // Cơ bản
    private Long id;
    private String fullName;
    private String specialization;
    private String bio;
    private String profileImageUrl;
    private String heroSubtitle;

    // Quick info (cho FE render 3 ô)
    private String ethnicity;      // Dân tộc
    private Integer age;           // Tính từ dateOfBirth hoặc user.dateOfBirth
    private String location;       // province.name

    // Gallery & narrative
    private List<String> images;
    private String panoramaImageUrl;
    private List<NarrativeBlock> narrativeContent;

    // Kết nối văn hóa
    private List<TourSummaryResponse> relatedTours;
    private List<CultureItemSummaryResponse> relatedCultureItems;
    private List<ArtisanSummaryResponse> otherArtisans;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NarrativeBlock {
        private String title;
        private String content;
        private String imageUrl;
    }
}
