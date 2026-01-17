package swd.coiviet.controller;

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
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.Province;
import swd.coiviet.model.Tour;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;
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

    public TourController(TourService tourService, CloudinaryService cloudinaryService, ProvinceService provinceService, ArtisanService artisanService) {
        this.tourService = tourService;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
        this.artisanService = artisanService;
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

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo tour mới", description = "Tạo tour với thông tin và ảnh (thumbnail và images)")
    public ResponseEntity<ApiResponse<Tour>> createTour(
            @Parameter(description = "ID tỉnh thành", required = true)
            @RequestParam @NotNull(message = "Province ID không được để trống") Long provinceId,
            @Parameter(description = "Tiêu đề tour", required = true)
            @RequestParam @NotBlank(message = "Tiêu đề không được để trống") String title,
            @Parameter(description = "Slug của tour", required = false)
            @RequestParam(required = false) String slug,
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Số giờ tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal durationHours,
            @Parameter(description = "Số người tham gia tối đa", required = false)
            @RequestParam(required = false) Integer maxParticipants,
            @Parameter(description = "Giá tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal price,
            @Parameter(description = "ID nghệ nhân", required = false)
            @RequestParam(required = false) Long artisanId,
            @Parameter(description = "Thumbnail image của tour", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh của tour (có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            // Get province
            Province province = provinceService.findById(provinceId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
            
            // Get artisan if provided
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
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Số giờ tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal durationHours,
            @Parameter(description = "Số người tham gia tối đa", required = false)
            @RequestParam(required = false) Integer maxParticipants,
            @Parameter(description = "Giá tour", required = false)
            @RequestParam(required = false) java.math.BigDecimal price,
            @Parameter(description = "ID nghệ nhân", required = false)
            @RequestParam(required = false) Long artisanId,
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
            if (durationHours != null) existing.setDurationHours(durationHours);
            if (maxParticipants != null) existing.setMaxParticipants(maxParticipants);
            if (price != null) existing.setPrice(price);
            if (artisanId != null) {
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
            
            Tour updated = tourService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật tour thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTour(@PathVariable Long id) {
        tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        tourService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tour thành công"));
    }
}
