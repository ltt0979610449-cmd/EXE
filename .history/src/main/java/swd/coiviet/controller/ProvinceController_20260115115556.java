package swd.coiviet.controller;

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

    @PostMapping
    public ResponseEntity<ApiResponse<Province>> createProvince(
            @RequestPart("province") Province province,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        try {
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = cloudinaryService.uploadProvinceThumbnail(thumbnail, null);
                province.setThumbnailUrl(thumbnailUrl);
            }
            
            Province saved = provinceService.save(province);
            
            // Re-upload with proper folder if needed
            if (saved.getId() != null && thumbnail != null && !thumbnail.isEmpty() && saved.getThumbnailUrl() == null) {
                String thumbnailUrl = cloudinaryService.uploadProvinceThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(thumbnailUrl);
                saved = provinceService.save(saved);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo tỉnh thành thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Province>> updateProvince(
            @PathVariable Long id,
            @RequestPart("province") Province province,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Province existing = provinceService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
        
        try {
            if (thumbnail != null && !thumbnail.isEmpty()) {
                // Delete old thumbnail
                if (existing.getThumbnailUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getThumbnailUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String thumbnailUrl = cloudinaryService.uploadProvinceThumbnail(thumbnail, id);
                province.setThumbnailUrl(thumbnailUrl);
            } else {
                province.setThumbnailUrl(existing.getThumbnailUrl());
            }
            
            province.setId(existing.getId());
            Province updated = provinceService.save(province);
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
