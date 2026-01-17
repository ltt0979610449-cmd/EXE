package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.HomePageResponse;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.enums.Status;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.BlogPost;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Tour;
import swd.coiviet.model.Video;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.BlogPostService;
import swd.coiviet.service.CultureItemService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.TourService;
import swd.coiviet.service.VideoService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/home")
public class HomeController {

    private final ProvinceService provinceService;
    private final TourService tourService;
    private final BlogPostService blogPostService;
    private final VideoService videoService;
    private final ArtisanService artisanService;
    private final CultureItemService cultureItemService;

    public HomeController(ProvinceService provinceService, TourService tourService,
                         BlogPostService blogPostService, VideoService videoService,
                         ArtisanService artisanService, CultureItemService cultureItemService) {
        this.provinceService = provinceService;
        this.tourService = tourService;
        this.blogPostService = blogPostService;
        this.videoService = videoService;
        this.artisanService = artisanService;
        this.cultureItemService = cultureItemService;
    }

    @GetMapping
    @Operation(summary = "Lấy dữ liệu cho trang chủ", description = "Lấy tất cả dữ liệu cần thiết cho trang chủ")
    public ResponseEntity<ApiResponse<HomePageResponse>> getHomePageData(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        // Get provinces for map
        List<swd.coiviet.model.Province> provinces = provinceService.findAll();
        
        // Get featured tours (active tours, sorted by rating and bookings)
        List<Tour> featuredTours = tourService.findAll().stream()
                .filter(t -> t.getStatus() == Status.ACTIVE)
                .sorted((a, b) -> {
                    // Sort by rating first, then by total bookings
                    java.math.BigDecimal ratingA = a.getAverageRating() != null ? a.getAverageRating() : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal ratingB = b.getAverageRating() != null ? b.getAverageRating() : java.math.BigDecimal.ZERO;
                    int ratingCompare = ratingB.compareTo(ratingA);
                    if (ratingCompare != 0) return ratingCompare;
                    Integer bookingsA = a.getTotalBookings() != null ? a.getTotalBookings() : 0;
                    Integer bookingsB = b.getTotalBookings() != null ? b.getTotalBookings() : 0;
                    return bookingsB.compareTo(bookingsA);
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        // Get featured blog posts (published, sorted by published date)
        List<BlogPost> featuredBlogs = blogPostService.findAll().stream()
                .filter(b -> b.getStatus() == PublicationStatus.PUBLISHED)
                .sorted((a, b) -> {
                    if (a.getPublishedAt() == null) return 1;
                    if (b.getPublishedAt() == null) return -1;
                    return b.getPublishedAt().compareTo(a.getPublishedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        // Get featured videos (published, sorted by created date)
        List<Video> featuredVideos = videoService.findByStatus(PublicationStatus.PUBLISHED).stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        // Get featured artisans (active, sorted by rating)
        List<Artisan> featuredArtisans = artisanService.findAll().stream()
                .filter(a -> a.getIsActive() != null && a.getIsActive())
                .sorted((a, b) -> {
                    java.math.BigDecimal ratingA = a.getAverageRating() != null ? a.getAverageRating() : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal ratingB = b.getAverageRating() != null ? b.getAverageRating() : java.math.BigDecimal.ZERO;
                    return ratingB.compareTo(ratingA);
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        // Get featured culture items (published, sorted by created date)
        List<CultureItem> featuredCultureItems = cultureItemService.findAll().stream()
                .filter(c -> c.getStatus() == PublicationStatus.PUBLISHED)
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        HomePageResponse response = HomePageResponse.builder()
                .provinces(provinces)
                .featuredTours(featuredTours)
                .featuredBlogs(featuredBlogs)
                .featuredVideos(featuredVideos)
                .featuredArtisans(featuredArtisans)
                .featuredCultureItems(featuredCultureItems)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
