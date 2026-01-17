package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.CreateTourRequest;
import swd.coiviet.dto.request.UpdateTourRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Province;
import swd.coiviet.model.Tour;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.TourService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;
    private final CloudinaryService cloudinaryService;
    private final ProvinceService provinceService;

    public TourController(TourService tourService, CloudinaryService cloudinaryService, ProvinceService provinceService) {
        this.tourService = tourService;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
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

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo tour mới", description = "Tạo tour với thông tin và ảnh (thumbnail và images)")
    public ResponseEntity<ApiResponse<Tour>> createTour(
            @ModelAttribute @Validated CreateTourRequest request,
            @Parameter(description = "Thumbnail image của tour", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh của tour", schema = @Schema(type = "array", format = "binary"))
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            // Get province
            Province province = provinceService.findById(request.getProvinceId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
            
            // Create tour entity
            Tour tour = Tour.builder()
                    .province(province)
                    .title(request.getTitle())
                    .slug(request.getSlug())
                    .description(request.getDescription())
                    .durationHours(request.getDurationHours())
                    .maxParticipants(request.getMaxParticipants())
                    .price(request.getPrice())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // Set artisan if provided
            if (request.getArtisanId() != null) {
                // You may need to inject ArtisanService if needed
                // For now, we'll skip this
            }
            
            Tour saved = tourService.save(tour);
            
            // Upload thumbnail if provided
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = cloudinaryService.uploadTourThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(thumbnailUrl);
            }
            
            // Upload images if provided
            if (images != null && images.length > 0) {
                List<String> imageUrls = cloudinaryService.uploadTourImages(images, saved.getId());
                String imagesJson = String.join(",", imageUrls);
                saved.setImages(imagesJson);
            }
            
            saved = tourService.save(saved);
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo tour thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Tour>> updateTour(
            @PathVariable Long id,
            @ModelAttribute @Validated UpdateTourRequest request,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        Tour existing = tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        
        try {
            // Update fields
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            if (request.getTitle() != null) existing.setTitle(request.getTitle());
            if (request.getSlug() != null) existing.setSlug(request.getSlug());
            if (request.getDescription() != null) existing.setDescription(request.getDescription());
            if (request.getDurationHours() != null) existing.setDurationHours(request.getDurationHours());
            if (request.getMaxParticipants() != null) existing.setMaxParticipants(request.getMaxParticipants());
            if (request.getPrice() != null) existing.setPrice(request.getPrice());
            
            // Handle thumbnail
            if (thumbnail != null && !thumbnail.isEmpty()) {
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
                List<String> imageUrls = cloudinaryService.uploadTourImages(images, id);
                String imagesJson = String.join(",", imageUrls);
                existing.setImages(imagesJson);
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
