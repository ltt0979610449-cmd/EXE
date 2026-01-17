package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Province;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/provinces")
public class ProvinceController {

    private final ProvinceService provinceService;
    private final CloudinaryService cloudinaryService;

    public ProvinceController(ProvinceService provinceService, CloudinaryService cloudinaryService) {
        this.provinceService = provinceService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<Province>>> getAllProvinces() {
        List<Province> provinces = provinceService.findAll();
        return ResponseEntity.ok(ApiResponse.success(provinces));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<Province>> getProvinceById(@PathVariable Long id) {
        Province province = provinceService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(province));
    }

    @GetMapping("/public/slug/{slug}")
    public ResponseEntity<ApiResponse<Province>> getProvinceBySlug(@PathVariable String slug) {
        Province province = provinceService.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(province));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo tỉnh thành mới", description = "Tạo tỉnh thành với thông tin và thumbnail")
    public ResponseEntity<ApiResponse<Province>> createProvince(
            @Parameter(description = "Tên tỉnh thành", required = true)
            @RequestParam @NotBlank(message = "Tên tỉnh thành không được để trống") String name,
            @Parameter(description = "Slug của tỉnh thành", required = false)
            @RequestParam(required = false) String slug,
            @Parameter(description = "Vùng miền", required = false)
            @RequestParam(required = false) String region,
            @Parameter(description = "Vĩ độ", required = false)
            @RequestParam(required = false) java.math.BigDecimal latitude,
            @Parameter(description = "Kinh độ", required = false)
            @RequestParam(required = false) java.math.BigDecimal longitude,
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Trạng thái hoạt động", required = false)
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Thumbnail image của tỉnh thành", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        try {
            // Debug logging for thumbnail
            if (thumbnail != null) {
                System.out.println("DEBUG: Thumbnail received - Name: " + thumbnail.getOriginalFilename() + 
                        ", Size: " + thumbnail.getSize() + 
                        ", ContentType: " + thumbnail.getContentType() + 
                        ", Empty: " + thumbnail.isEmpty());
            } else {
                System.out.println("DEBUG: Thumbnail is NULL - Check if file field name is 'thumbnail'");
            }
            
            Province province = Province.builder()
                    .name(name)
                    .slug(slug)
                    .region(region)
                    .latitude(latitude)
                    .longitude(longitude)
                    .description(description)
                    .isActive(isActive != null ? isActive : true)
                    .build();
            
            Province saved = provinceService.save(province);
            
            // Upload thumbnail if provided
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
                System.out.println("DEBUG: Uploading thumbnail to Cloudinary...");
                String thumbnailUrl = cloudinaryService.uploadProvinceThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(thumbnailUrl);
                saved = provinceService.save(saved);
                System.out.println("DEBUG: Thumbnail uploaded successfully: " + thumbnailUrl);
            } else {
                System.out.println("DEBUG: Thumbnail not uploaded - null: " + (thumbnail == null) + 
                        ", empty: " + (thumbnail != null && thumbnail.isEmpty()) + 
                        ", size: " + (thumbnail != null ? thumbnail.getSize() : "N/A"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo tỉnh thành thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật tỉnh thành", description = "Cập nhật thông tin tỉnh thành và thumbnail")
    public ResponseEntity<ApiResponse<Province>> updateProvince(
            @PathVariable Long id,
            @Parameter(description = "Tên tỉnh thành", required = false)
            @RequestParam(required = false) String name,
            @Parameter(description = "Slug của tỉnh thành", required = false)
            @RequestParam(required = false) String slug,
            @Parameter(description = "Vùng miền", required = false)
            @RequestParam(required = false) String region,
            @Parameter(description = "Vĩ độ", required = false)
            @RequestParam(required = false) java.math.BigDecimal latitude,
            @Parameter(description = "Kinh độ", required = false)
            @RequestParam(required = false) java.math.BigDecimal longitude,
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "Trạng thái hoạt động", required = false)
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Thumbnail image mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Province existing = provinceService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
        
        try {
            // Update fields
            if (name != null) existing.setName(name);
            if (slug != null) existing.setSlug(slug);
            if (region != null) existing.setRegion(region);
            if (latitude != null) existing.setLatitude(latitude);
            if (longitude != null) existing.setLongitude(longitude);
            if (description != null) existing.setDescription(description);
            if (isActive != null) existing.setIsActive(isActive);
            
            // Handle thumbnail
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
                // Delete old thumbnail
                if (existing.getThumbnailUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getThumbnailUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String thumbnailUrl = cloudinaryService.uploadProvinceThumbnail(thumbnail, id);
                existing.setThumbnailUrl(thumbnailUrl);
            }
            
            Province updated = provinceService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật tỉnh thành thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProvince(@PathVariable Long id) {
        provinceService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
        provinceService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tỉnh thành thành công"));
    }

}
