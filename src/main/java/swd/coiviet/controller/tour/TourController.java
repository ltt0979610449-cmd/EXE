package swd.coiviet.controller.tour;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.TourDetailResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.enums.CultureCategory;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Province;
import swd.coiviet.model.Tour;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.TourCultureItemService;
import swd.coiviet.service.TourService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;
    private final CloudinaryService cloudinaryService;
    private final ProvinceService provinceService;
    private final ArtisanService artisanService;
    private final TourCultureItemService tourCultureItemService;

    public TourController(TourService tourService, CloudinaryService cloudinaryService, ProvinceService provinceService, ArtisanService artisanService, TourCultureItemService tourCultureItemService) {
        this.tourService = tourService;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
        this.artisanService = artisanService;
        this.tourCultureItemService = tourCultureItemService;
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<Tour>>> getAllTours() {
        List<Tour> tours = tourService.findAll();
        return ResponseEntity.ok(ApiResponse.success(tours));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<Tour>> getTourById(@PathVariable Long id) {
        Tour tour = tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(tour));
    }

    @GetMapping("/public/{id}/detail")
    @Operation(summary = "Lấy chi tiết tour kèm culture items", description = "Trả về tour và danh sách culture items (địa điểm nổi bật, lễ hội, ẩm thực). Fallback theo province nếu tour chưa gắn items.")
    public ResponseEntity<ApiResponse<TourDetailResponse>> getTourDetail(@PathVariable Long id) {
        Tour tour = tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        List<CultureItem> cultureItems = tourCultureItemService.findCultureItemsByTourId(id);
        TourDetailResponse response = TourDetailResponse.builder()
                .tour(tour)
                .cultureItems(cultureItems)
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public/province/{provinceId}")
    public ResponseEntity<ApiResponse<List<Tour>>> getToursByProvince(@PathVariable Long provinceId) {
        List<Tour> tours = tourService.findByProvinceId(provinceId);
        return ResponseEntity.ok(ApiResponse.success(tours));
    }

    @GetMapping("/public/artisan/{artisanId}")
    public ResponseEntity<ApiResponse<List<Tour>>> getToursByArtisan(@PathVariable Long artisanId) {
        List<Tour> tours = tourService.findByArtisanId(artisanId);
        return ResponseEntity.ok(ApiResponse.success(tours));
    }

    @GetMapping("/public/{id}/culture-items")
    @Operation(summary = "Lấy culture items của tour", description = "Lấy địa điểm nổi bật, lễ hội, ẩm thực gắn với tour. Nếu tour chưa gắn items → fallback theo province. Có thể filter theo category.")
    public ResponseEntity<ApiResponse<List<CultureItem>>> getTourCultureItems(
            @PathVariable Long id,
            @RequestParam(required = false) CultureCategory category) {
        tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        List<CultureItem> items = tourCultureItemService.findCultureItemsByTourIdAndCategory(id, category);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/public/{id}/highlights")
    @Operation(summary = "Lấy địa điểm nổi bật của tour", description = "Alias cho culture-items không filter. Fallback theo province nếu tour chưa gắn items.")
    public ResponseEntity<ApiResponse<List<CultureItem>>> getTourHighlights(@PathVariable Long id) {
        return getTourCultureItems(id, null);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo tour mới", description = "Tạo tour với thông tin và ảnh (thumbnail và images)")
    public ResponseEntity<ApiResponse<Tour>> createTour(
            @Parameter(description = "ID tỉnh thành", required = true)
            @RequestParam @NotNull(message = "Province ID không được để trống") Long provinceId,
            @Parameter(description = "Tiêu đề tour", required = true)
            @RequestParam @NotBlank(message = "Tiêu đề không được để trống") String title,
            @Parameter(description = "Slug của tour", required = false)
            @RequestParam(required = false) String slug,
            @Parameter(description = "Mô tả / Giới thiệu chung", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Thời điểm đẹp nhất (e.g. Tháng 10 - Tháng 3 mùa khô)", required = false)
            @RequestParam(required = false) String bestSeason,
            @Parameter(description = "Cách di chuyển đến vùng (e.g. Xe máy, xe khách từ Pleiku)", required = false)
            @RequestParam(required = false) String transportation,
            @Parameter(description = "Lưu ý ứng xử văn hoá - JSON array hoặc text", required = false)
            @RequestParam(required = false) String culturalTips,
            @Parameter(description = "Số giờ tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal durationHours,
            @Parameter(description = "Số người tham gia tối đa", required = false)
            @RequestParam(required = false) Integer maxParticipants,
            @Parameter(description = "Giá tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal price,
            @Parameter(description = "ID nghệ nhân (tùy chọn - không truyền thì tour không có nghệ nhân hướng dẫn)")
            @RequestParam(required = false) Long artisanId,
            @Parameter(description = "Danh sách ID culture items gắn với tour (địa điểm nổi bật, lễ hội, ẩm thực). Thứ tự = thứ tự hiển thị.")
            @RequestParam(required = false) List<Long> cultureItemIds,
            @Parameter(description = "Thumbnail image của tour", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh của tour (có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            // Get province
            Province province = provinceService.findById(provinceId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));

            // Get artisan (optional)
            Artisan artisan = null;
            if (artisanId != null) {
                artisan = artisanService.findById(artisanId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
            }
            
            // Create tour entity
            Tour tour = Tour.builder()
                    .province(province)
                    .title(title)
                    .slug(slug)
                    .description(description)
                    .bestSeason(bestSeason)
                    .transportation(transportation)
                    .culturalTips(culturalTips)
                    .durationHours(durationHours)
                    .maxParticipants(maxParticipants)
                    .price(price)
                    .artisan(artisan)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            Tour saved = tourService.save(tour);
            
            // Upload thumbnail if provided
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
                String thumbnailUrl = cloudinaryService.uploadTourThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(thumbnailUrl);
            }
            
            // Upload images if provided
            if (images != null && images.length > 0) {
                // Filter out empty files
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .collect(java.util.stream.Collectors.toList());
                
                if (!validImages.isEmpty()) {
                    MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                    List<String> imageUrls = cloudinaryService.uploadTourImages(validImagesArray, saved.getId());
                    String imagesJson = String.join(",", imageUrls);
                    saved.setImages(imagesJson);
                }
            }
            
            saved = tourService.save(saved);
            
            if (cultureItemIds != null && !cultureItemIds.isEmpty()) {
                tourCultureItemService.setCultureItemsForTour(saved.getId(), cultureItemIds);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo tour thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật tour", description = "Cập nhật thông tin tour và ảnh")
    public ResponseEntity<ApiResponse<Tour>> updateTour(
            @PathVariable Long id,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Tiêu đề tour", required = false)
            @RequestParam(required = false) String title,
            @Parameter(description = "Slug của tour", required = false)
            @RequestParam(required = false) String slug,
            @Parameter(description = "Mô tả / Giới thiệu chung", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Thời điểm đẹp nhất", required = false)
            @RequestParam(required = false) String bestSeason,
            @Parameter(description = "Cách di chuyển đến vùng", required = false)
            @RequestParam(required = false) String transportation,
            @Parameter(description = "Lưu ý ứng xử văn hoá - JSON array hoặc text", required = false)
            @RequestParam(required = false) String culturalTips,
            @Parameter(description = "Số giờ tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal durationHours,
            @Parameter(description = "Số người tham gia tối đa", required = false)
            @RequestParam(required = false) Integer maxParticipants,
            @Parameter(description = "Giá tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal price,
            @Parameter(description = "ID nghệ nhân", required = false)
            @RequestParam(required = false) Long artisanId,
            @Parameter(description = "Xóa nghệ nhân khỏi tour khi true")
            @RequestParam(required = false) Boolean clearArtisan,
            @Parameter(description = "Danh sách ID culture items (thay thế toàn bộ). Truyền rỗng để xóa hết.")
            @RequestParam(required = false) List<Long> cultureItemIds,
            @Parameter(description = "Thumbnail image mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh mới (nếu có, có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        Tour existing = tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        
        try {
            // Update fields
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            if (title != null) existing.setTitle(title);
            if (slug != null) existing.setSlug(slug);
            if (description != null) existing.setDescription(description);
            if (bestSeason != null) existing.setBestSeason(bestSeason);
            if (transportation != null) existing.setTransportation(transportation);
            if (culturalTips != null) existing.setCulturalTips(culturalTips);
            if (durationHours != null) existing.setDurationHours(durationHours);
            if (maxParticipants != null) existing.setMaxParticipants(maxParticipants);
            if (price != null) existing.setPrice(price);
            if (Boolean.TRUE.equals(clearArtisan)) {
                existing.setArtisan(null);
            } else if (artisanId != null) {
                Artisan artisan = artisanService.findById(artisanId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
                existing.setArtisan(artisan);
            }
            
            // Handle thumbnail
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
                // Delete old thumbnail if exists
                if (existing.getThumbnailUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getThumbnailUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String thumbnailUrl = cloudinaryService.uploadTourThumbnail(thumbnail, id);
                existing.setThumbnailUrl(thumbnailUrl);
            }
            
            // Handle images
            if (images != null && images.length > 0) {
                // Filter out empty files
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .collect(java.util.stream.Collectors.toList());
                
                if (!validImages.isEmpty()) {
                    // Delete old images if exists
                    if (existing.getImages() != null && !existing.getImages().isEmpty()) {
                        String[] oldImageUrls = existing.getImages().split(",");
                        for (String oldUrl : oldImageUrls) {
                            String publicId = cloudinaryService.extractPublicIdFromUrl(oldUrl.trim());
                            if (publicId != null) {
                                cloudinaryService.deleteResource(publicId);
                            }
                        }
                    }
                    MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                    List<String> imageUrls = cloudinaryService.uploadTourImages(validImagesArray, id);
                    String imagesJson = String.join(",", imageUrls);
                    existing.setImages(imagesJson);
                }
            }
            
            if (cultureItemIds != null) {
                tourCultureItemService.setCultureItemsForTour(id, cultureItemIds);
            }
            
            Tour updated = tourService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật tour thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/culture-items")
    @Operation(summary = "Gắn culture items vào tour", description = "Thay thế toàn bộ culture items của tour. Truyền rỗng để xóa hết. Cần role STAFF/ADMIN.")
    public ResponseEntity<ApiResponse<Void>> setTourCultureItems(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> cultureItemIds) {
        tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        tourCultureItemService.setCultureItemsForTour(id, cultureItemIds != null ? cultureItemIds : List.of());
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật culture items thành công"));
    }

    @DeleteMapping("/{id}/culture-items/{cultureItemId}")
    @Operation(summary = "Bỏ culture item khỏi tour", description = "Cần role STAFF/ADMIN.")
    public ResponseEntity<ApiResponse<Void>> removeTourCultureItem(
            @PathVariable Long id,
            @PathVariable Long cultureItemId) {
        tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        tourCultureItemService.deleteByTourIdAndCultureItemId(id, cultureItemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã bỏ culture item khỏi tour"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTour(@PathVariable Long id) {
        tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        tourService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tour thành công"));
    }
}
