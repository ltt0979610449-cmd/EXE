package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.CreateCultureItemRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Province;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.CultureItemService;
import swd.coiviet.service.ProvinceService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/culture-items")
public class CultureItemController {

    private final CultureItemService cultureItemService;
    private final CloudinaryService cloudinaryService;
    private final ProvinceService provinceService;

    public CultureItemController(CultureItemService cultureItemService, CloudinaryService cloudinaryService, ProvinceService provinceService) {
        this.cultureItemService = cultureItemService;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<CultureItem>>> getAllCultureItems() {
        // Need to add findAll method to service
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<CultureItem>> getCultureItemById(@PathVariable Long id) {
        CultureItem item = cultureItemService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn hóa không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/public/province/{provinceId}")
    public ResponseEntity<ApiResponse<List<CultureItem>>> getCultureItemsByProvince(@PathVariable Long provinceId) {
        List<CultureItem> items = cultureItemService.findAllByProvinceId(provinceId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo văn hóa mới", description = "Tạo văn hóa với thông tin, thumbnail và images")
    public ResponseEntity<ApiResponse<CultureItem>> createCultureItem(
            @ModelAttribute @Validated CreateCultureItemRequest request,
            @Parameter(description = "Thumbnail image", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh", schema = @Schema(type = "array", format = "binary"))
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            Province province = provinceService.findById(request.getProvinceId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
            
            CultureItem item = CultureItem.builder()
                    .province(province)
                    .category(request.getCategory())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .videoUrl(request.getVideoUrl())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            CultureItem saved = cultureItemService.save(item);
            
            // Upload thumbnail if provided
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = cloudinaryService.uploadCultureItemThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(thumbnailUrl);
            }
            
            // Upload images if provided
            if (images != null && images.length > 0) {
                List<String> imageUrls = cloudinaryService.uploadCultureItemImages(images, saved.getId());
                String imagesJson = String.join(",", imageUrls);
                saved.setImages(imagesJson);
            }
            
            saved = cultureItemService.save(saved);
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo văn hóa thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật văn hóa", description = "Cập nhật thông tin văn hóa và ảnh")
    public ResponseEntity<ApiResponse<CultureItem>> updateCultureItem(
            @PathVariable Long id,
            @ModelAttribute @Validated CreateCultureItemRequest request,
            @Parameter(description = "Thumbnail image mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh mới (nếu có)", schema = @Schema(type = "array", format = "binary"))
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        CultureItem existing = cultureItemService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn hóa không tồn tại"));
        
        try {
            // Update fields
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            if (request.getCategory() != null) existing.setCategory(request.getCategory());
            if (request.getTitle() != null) existing.setTitle(request.getTitle());
            if (request.getDescription() != null) existing.setDescription(request.getDescription());
            if (request.getVideoUrl() != null) existing.setVideoUrl(request.getVideoUrl());
            
            // Handle thumbnail
            if (thumbnail != null && !thumbnail.isEmpty()) {
                if (existing.getThumbnailUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getThumbnailUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String thumbnailUrl = cloudinaryService.uploadCultureItemThumbnail(thumbnail, id);
                existing.setThumbnailUrl(thumbnailUrl);
            }
            
            // Handle images
            if (images != null && images.length > 0) {
                if (existing.getImages() != null && !existing.getImages().isEmpty()) {
                    String[] oldImageUrls = existing.getImages().split(",");
                    for (String oldUrl : oldImageUrls) {
                        String publicId = cloudinaryService.extractPublicIdFromUrl(oldUrl.trim());
                        if (publicId != null) {
                            cloudinaryService.deleteResource(publicId);
                        }
                    }
                }
                List<String> imageUrls = cloudinaryService.uploadCultureItemImages(images, id);
                String imagesJson = String.join(",", imageUrls);
                existing.setImages(imagesJson);
            }
            
            CultureItem updated = cultureItemService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật văn hóa thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCultureItem(@PathVariable Long id) {
        cultureItemService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn hóa không tồn tại"));
        cultureItemService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa văn hóa thành công"));
    }
}
