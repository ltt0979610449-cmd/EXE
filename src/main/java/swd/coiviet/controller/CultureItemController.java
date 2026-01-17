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
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Province;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.CultureItemService;
import swd.coiviet.service.ProvinceService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    @Operation(summary = "Lấy danh sách văn hóa đã publish", description = "Lấy tất cả văn hóa đã được publish")
    public ResponseEntity<ApiResponse<List<CultureItem>>> getAllCultureItems() {
        List<CultureItem> items = cultureItemService.findByStatus(swd.coiviet.enums.PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
    
    @GetMapping("/public/category/{category}")
    @Operation(summary = "Lấy văn hóa theo category", description = "Lấy danh sách văn hóa theo loại (FESTIVAL, FOOD, COSTUME, etc.)")
    public ResponseEntity<ApiResponse<List<CultureItem>>> getCultureItemsByCategory(
            @PathVariable swd.coiviet.enums.CultureCategory category) {
        List<CultureItem> items = cultureItemService.findByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
    
    @GetMapping("/public/province/{provinceId}/category/{category}")
    @Operation(summary = "Lấy văn hóa theo tỉnh và category", description = "Lấy danh sách văn hóa của một tỉnh theo loại")
    public ResponseEntity<ApiResponse<List<CultureItem>>> getCultureItemsByProvinceAndCategory(
            @PathVariable Long provinceId,
            @PathVariable swd.coiviet.enums.CultureCategory category) {
        List<CultureItem> items = cultureItemService.findByProvinceIdAndCategory(provinceId, category);
        return ResponseEntity.ok(ApiResponse.success(items));
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
            @Parameter(description = "ID tỉnh thành", required = true)
            @RequestParam @NotNull(message = "Province ID không được để trống") Long provinceId,
            @Parameter(description = "Category", required = true)
            @RequestParam @NotNull(message = "Category không được để trống") swd.coiviet.enums.CultureCategory category,
            @Parameter(description = "Tiêu đề", required = true)
            @RequestParam @NotBlank(message = "Tiêu đề không được để trống") String title,
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Video URL", required = false)
            @RequestParam(required = false) String videoUrl,
            @Parameter(description = "Thumbnail image", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh (có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            Province province = provinceService.findById(provinceId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
            
            CultureItem item = CultureItem.builder()
                    .province(province)
                    .category(category)
                    .title(title)
                    .description(description)
                    .videoUrl(videoUrl)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            CultureItem saved = cultureItemService.save(item);
            
            // Upload thumbnail if provided
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
                String thumbnailUrl = cloudinaryService.uploadCultureItemThumbnail(thumbnail, saved.getId());
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
                    List<String> imageUrls = cloudinaryService.uploadCultureItemImages(validImagesArray, saved.getId());
                    String imagesJson = String.join(",", imageUrls);
                    saved.setImages(imagesJson);
                }
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
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Category", required = false)
            @RequestParam(required = false) swd.coiviet.enums.CultureCategory category,
            @Parameter(description = "Tiêu đề", required = false)
            @RequestParam(required = false) String title,
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Video URL", required = false)
            @RequestParam(required = false) String videoUrl,
            @Parameter(description = "Thumbnail image mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "Danh sách ảnh mới (nếu có, có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        CultureItem existing = cultureItemService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn hóa không tồn tại"));
        
        try {
            // Update fields
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            if (category != null) existing.setCategory(category);
            if (title != null) existing.setTitle(title);
            if (description != null) existing.setDescription(description);
            if (videoUrl != null) existing.setVideoUrl(videoUrl);
            
            // Handle thumbnail
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
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
                // Filter out empty files
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .collect(java.util.stream.Collectors.toList());
                
                if (!validImages.isEmpty()) {
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
                    List<String> imageUrls = cloudinaryService.uploadCultureItemImages(validImagesArray, id);
                    String imagesJson = String.join(",", imageUrls);
                    existing.setImages(imagesJson);
                }
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
