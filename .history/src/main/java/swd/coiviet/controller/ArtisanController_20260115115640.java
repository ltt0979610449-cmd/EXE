package swd.coiviet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.CreateArtisanRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.Province;
import swd.coiviet.model.User;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/artisans")
public class ArtisanController {

    private final ArtisanService artisanService;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final ProvinceService provinceService;

    public ArtisanController(ArtisanService artisanService, CloudinaryService cloudinaryService, UserService userService, ProvinceService provinceService) {
        this.artisanService = artisanService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
        this.provinceService = provinceService;
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<Artisan>>> getAllArtisans() {
        // Need to add findAll method to service
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<Artisan>> getArtisanById(@PathVariable Long id) {
        Artisan artisan = artisanService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(artisan));
    }

    @GetMapping("/public/province/{provinceId}")
    public ResponseEntity<ApiResponse<List<Artisan>>> getArtisansByProvince(@PathVariable Long provinceId) {
        List<Artisan> artisans = artisanService.findByProvinceId(provinceId);
        return ResponseEntity.ok(ApiResponse.success(artisans));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Artisan>> createArtisan(
            @RequestPart("artisan") Artisan artisan,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, null);
                artisan.setProfileImageUrl(profileImageUrl);
            }
            
            Artisan saved = artisanService.save(artisan);
            
            // Re-upload with proper folder if needed
            if (saved.getId() != null) {
                if (profileImage != null && !profileImage.isEmpty() && saved.getProfileImageUrl() == null) {
                    String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, saved.getId());
                    saved.setProfileImageUrl(profileImageUrl);
                }
                saved = artisanService.save(saved);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo nghệ nhân thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Artisan>> updateArtisan(
            @PathVariable Long id,
            @RequestPart("artisan") Artisan artisan,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        Artisan existing = artisanService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
        
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                if (existing.getProfileImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getProfileImageUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, id);
                artisan.setProfileImageUrl(profileImageUrl);
            } else {
                artisan.setProfileImageUrl(existing.getProfileImageUrl());
            }
            
            artisan.setId(existing.getId());
            Artisan updated = artisanService.save(artisan);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật nghệ nhân thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArtisan(@PathVariable Long id) {
        artisanService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
        artisanService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa nghệ nhân thành công"));
    }
}
