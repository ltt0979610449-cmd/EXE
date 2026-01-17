package swd.coiviet.controller;

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
    public ResponseEntity<ApiResponse<Tour>> createTour(
            @ModelAttribute @Validated CreateTourRequest request,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Tour>> updateTour(
            @PathVariable Long id,
            @RequestPart("tour") Tour tour,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        Tour existing = tourService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        
        try {
            // Delete old images if new ones are provided
            if (thumbnail != null && !thumbnail.isEmpty()) {
                // Delete old thumbnail if exists
                if (existing.getThumbnailUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getThumbnailUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String thumbnailUrl = cloudinaryService.uploadTourThumbnail(thumbnail, id);
                tour.setThumbnailUrl(thumbnailUrl);
            } else {
                tour.setThumbnailUrl(existing.getThumbnailUrl());
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
                tour.setImages(imagesJson);
            } else {
                tour.setImages(existing.getImages());
            }
            
            tour.setId(existing.getId());
            Tour updated = tourService.save(tour);
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
