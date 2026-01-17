package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.BlogPost;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Province;
import swd.coiviet.model.Tour;
import swd.coiviet.model.Video;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageResponse {
    private List<Province> provinces; // For map
    private List<Tour> featuredTours; // Bán tour trải nghiệm văn hóa
    private List<BlogPost> featuredBlogs; // Học nhanh văn hóa Tây Nguyên
    private List<Video> featuredVideos; // Video ngắn
    private List<Artisan> featuredArtisans; // Góc nghệ nhân
    private List<CultureItem> featuredCultureItems; // Văn hóa nổi bật
}
