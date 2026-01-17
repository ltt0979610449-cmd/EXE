package swd.coiviet.controller;

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

    public CultureItemController(CultureItemService cultureItemService, CloudinaryService cloudinaryService) {
        this.cultureItemService = cultureItemService;
        this.cloudinaryService = cloudinaryService;
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

    @PostMapping
    public ResponseEntity<ApiResponse<CultureItem>> createCultureItem(
            @RequestPart("cultureItem") CultureItem item,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = cloudinaryService.uploadCultureItemThumbnail(thumbnail, null);
                item.setThumbnailUrl(thumbnailUrl);
            }
            
            if (images != null && images.length > 0) {
                List<String> imageUrls = cloudinaryService.uploadCultureItemImages(images, null);
                String imagesJson = String.join(",", imageUrls);
                item.setImages(imagesJson);
            }
            
            CultureItem saved = cultureItemService.save(item);
            
            // Re-upload with proper folder if needed
            if (saved.getId() != null) {
                if (thumbnail != null && !thumbnail.isEmpty() && saved.getThumbnailUrl() == null) {
                    String thumbnailUrl = cloudinaryService.uploadCultureItemThumbnail(thumbnail, saved.getId());
                    saved.setThumbnailUrl(thumbnailUrl);
                }
                if (images != null && images.length > 0 && saved.getImages() == null) {
                    List<String> imageUrls = cloudinaryService.uploadCultureItemImages(images, saved.getId());
                    String imagesJson = String.join(",", imageUrls);
                    saved.setImages(imagesJson);
                }
                saved = cultureItemService.save(saved);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo văn hóa thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CultureItem>> updateCultureItem(
            @PathVariable Long id,
            @RequestPart("cultureItem") CultureItem item,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        CultureItem existing = cultureItemService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn hóa không tồn tại"));
        
        try {
            if (thumbnail != null && !thumbnail.isEmpty()) {
                if (existing.getThumbnailUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getThumbnailUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String thumbnailUrl = cloudinaryService.uploadCultureItemThumbnail(thumbnail, id);
                item.setThumbnailUrl(thumbnailUrl);
            } else {
                item.setThumbnailUrl(existing.getThumbnailUrl());
            }
            
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
                item.setImages(imagesJson);
            } else {
                item.setImages(existing.getImages());
            }
            
            item.setId(existing.getId());
            CultureItem updated = cultureItemService.save(item);
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
