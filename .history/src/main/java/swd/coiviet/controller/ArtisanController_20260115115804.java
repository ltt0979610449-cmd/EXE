package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo nghệ nhân mới", description = "Tạo nghệ nhân với thông tin và ảnh profile")
    public ResponseEntity<ApiResponse<Artisan>> createArtisan(
            @ModelAttribute @Validated CreateArtisanRequest request,
            @Parameter(description = "Ảnh profile của nghệ nhân", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @Parameter(description = "Danh sách ảnh khác", schema = @Schema(type = "array", format = "binary"))
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            User user = userService.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User không tồn tại"));
            
            Artisan artisan = Artisan.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .specialization(request.getSpecialization())
                    .bio(request.getBio())
                    .workshopAddress(request.getWorkshopAddress())
                    .createdAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                artisan.setProvince(province);
            }
            
            Artisan saved = artisanService.save(artisan);
            
            // Upload profile image if provided
            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, saved.getId());
                saved.setProfileImageUrl(profileImageUrl);
                saved = artisanService.save(saved);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo nghệ nhân thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Artisan>> updateArtisan(
            @PathVariable Long id,
            @ModelAttribute @Validated CreateArtisanRequest request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        Artisan existing = artisanService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
        
        try {
            // Update fields
            if (request.getFullName() != null) existing.setFullName(request.getFullName());
            if (request.getSpecialization() != null) existing.setSpecialization(request.getSpecialization());
            if (request.getBio() != null) existing.setBio(request.getBio());
            if (request.getWorkshopAddress() != null) existing.setWorkshopAddress(request.getWorkshopAddress());
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            
            // Handle profile image
            if (profileImage != null && !profileImage.isEmpty()) {
                if (existing.getProfileImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getProfileImageUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, id);
                existing.setProfileImageUrl(profileImageUrl);
            }
            
            Artisan updated = artisanService.save(existing);
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
