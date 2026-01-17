package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.CreateProvinceRequest;
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
            @ModelAttribute @Validated CreateProvinceRequest request,
            @Parameter(description = "Thumbnail image của tỉnh thành", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        try {
            Province province = Province.builder()
                    .name(request.getName())
                    .slug(request.getSlug())
                    .region(request.getRegion())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .description(request.getDescription())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .build();
            
            Province saved = provinceService.save(province);
            
            // Upload thumbnail if provided
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = cloudinaryService.uploadProvinceThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(thumbnailUrl);
                saved = provinceService.save(saved);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo tỉnh thành thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Province>> updateProvince(
            @PathVariable Long id,
            @ModelAttribute @Validated CreateProvinceRequest request,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Province existing = provinceService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
        
        try {
            // Update fields
            if (request.getName() != null) existing.setName(request.getName());
            if (request.getSlug() != null) existing.setSlug(request.getSlug());
            if (request.getRegion() != null) existing.setRegion(request.getRegion());
            if (request.getLatitude() != null) existing.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) existing.setLongitude(request.getLongitude());
            if (request.getDescription() != null) existing.setDescription(request.getDescription());
            if (request.getIsActive() != null) existing.setIsActive(request.getIsActive());
            
            // Handle thumbnail
            if (thumbnail != null && !thumbnail.isEmpty()) {
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
