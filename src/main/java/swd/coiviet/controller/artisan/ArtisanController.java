package swd.coiviet.controller.artisan;

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
import swd.coiviet.dto.response.ArtisanDetailResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.enums.Role;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.Province;
import swd.coiviet.model.User;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
    @Operation(summary = "Lấy danh sách nghệ nhân", description = "Lấy tất cả nghệ nhân đang hoạt động")
    public ResponseEntity<ApiResponse<List<Artisan>>> getAllArtisans() {
        List<Artisan> artisans = artisanService.findAll().stream()
                .filter(a -> a.getIsActive() != null && a.getIsActive())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(artisans));
    }

    @GetMapping("/public/{id}/detail")
    @Operation(summary = "Lấy chi tiết nghệ nhân", description = "Trả về thông tin đầy đủ cho trang artisan: quick info, gallery, narrative, tours/culture liên quan")
    public ResponseEntity<ApiResponse<ArtisanDetailResponse>> getArtisanDetail(@PathVariable Long id) {
        ArtisanDetailResponse detail = artisanService.getDetailById(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
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
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull(message = "User ID không được để trống") Long userId,
            @Parameter(description = "Họ tên", required = true)
            @RequestParam @NotBlank(message = "Họ tên không được để trống") String fullName,
            @Parameter(description = "Chuyên môn", required = true)
            @RequestParam @NotBlank(message = "Chuyên môn không được để trống") String specialization,
            @Parameter(description = "Tiểu sử", required = false)
            @RequestParam(required = false) String bio,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Địa chỉ xưởng", required = false)
            @RequestParam(required = false) String workshopAddress,
            @Parameter(description = "Dân tộc (vd: Mường, Jrai)", required = false)
            @RequestParam(required = false) String ethnicity,
            @Parameter(description = "Ngày sinh (yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String dateOfBirth,
            @Parameter(description = "Mô tả ngắn cho hero", required = false)
            @RequestParam(required = false) String heroSubtitle,
            @Parameter(description = "Narrative JSON: [{\"title\":\"...\",\"content\":\"...\",\"imageUrl\":\"...\"}]", required = false)
            @RequestParam(required = false) String narrativeContent,
            @Parameter(description = "Ảnh profile của nghệ nhân", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @Parameter(description = "Ảnh panorama full-width", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "panoramaImage", required = false) MultipartFile panoramaImage,
            @Parameter(description = "Danh sách ảnh khác (có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User không tồn tại"));
            
            Artisan artisan = Artisan.builder()
                    .user(user)
                    .fullName(fullName)
                    .specialization(specialization)
                    .bio(bio)
                    .workshopAddress(workshopAddress)
                    .ethnicity(ethnicity)
                    .heroSubtitle(heroSubtitle)
                    .narrativeContent(narrativeContent)
                    .createdAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            
            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                try {
                    artisan.setDateOfBirth(LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (Exception ignored) { }
            }
            
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                artisan.setProvince(province);
            }
            
            Artisan saved = artisanService.save(artisan);

            // Đổi role của user thành ARTISAN khi tạo artisan thành công
            user.setRole(Role.ARTISAN);
            userService.save(user);
            
            // Upload profile image if provided
            if (profileImage != null && !profileImage.isEmpty() && profileImage.getSize() > 0) {
                String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, saved.getId());
                saved.setProfileImageUrl(profileImageUrl);
                saved = artisanService.save(saved);
            }
            
            // Upload panorama image if provided
            if (panoramaImage != null && !panoramaImage.isEmpty() && panoramaImage.getSize() > 0) {
                String panoramaUrl = cloudinaryService.uploadArtisanPanoramaImage(panoramaImage, saved.getId());
                saved.setPanoramaImageUrl(panoramaUrl);
                saved = artisanService.save(saved);
            }
            
            // Upload gallery images if provided
            if (images != null && images.length > 0) {
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .collect(java.util.stream.Collectors.toList());
                if (!validImages.isEmpty()) {
                    MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                    List<String> imageUrls = cloudinaryService.uploadArtisanImages(validImagesArray, saved.getId());
                    String imagesJson = String.join(",", imageUrls);
                    saved.setImages(imagesJson);
                    saved = artisanService.save(saved);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo nghệ nhân thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật nghệ nhân", description = "Cập nhật thông tin nghệ nhân và ảnh")
    public ResponseEntity<ApiResponse<Artisan>> updateArtisan(
            @PathVariable Long id,
            @Parameter(description = "Họ tên", required = false)
            @RequestParam(required = false) String fullName,
            @Parameter(description = "Chuyên môn", required = false)
            @RequestParam(required = false) String specialization,
            @Parameter(description = "Tiểu sử", required = false)
            @RequestParam(required = false) String bio,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Địa chỉ xưởng", required = false)
            @RequestParam(required = false) String workshopAddress,
            @Parameter(description = "Dân tộc (vd: Mường, Jrai)", required = false)
            @RequestParam(required = false) String ethnicity,
            @Parameter(description = "Ngày sinh (yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String dateOfBirth,
            @Parameter(description = "Mô tả ngắn cho hero", required = false)
            @RequestParam(required = false) String heroSubtitle,
            @Parameter(description = "Narrative JSON: [{\"title\":\"...\",\"content\":\"...\",\"imageUrl\":\"...\"}]", required = false)
            @RequestParam(required = false) String narrativeContent,
            @Parameter(description = "Ảnh profile mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @Parameter(description = "Ảnh panorama full-width (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "panoramaImage", required = false) MultipartFile panoramaImage,
            @Parameter(description = "Danh sách ảnh mới (nếu có, có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        Artisan existing = artisanService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
        
        try {
            // Update fields
            if (fullName != null) existing.setFullName(fullName);
            if (specialization != null) existing.setSpecialization(specialization);
            if (bio != null) existing.setBio(bio);
            if (workshopAddress != null) existing.setWorkshopAddress(workshopAddress);
            if (ethnicity != null) existing.setEthnicity(ethnicity);
            if (heroSubtitle != null) existing.setHeroSubtitle(heroSubtitle);
            if (narrativeContent != null) existing.setNarrativeContent(narrativeContent);
            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                try {
                    existing.setDateOfBirth(LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (Exception ignored) { }
            }
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            
            // Handle profile image
            if (profileImage != null && !profileImage.isEmpty() && profileImage.getSize() > 0) {
                if (existing.getProfileImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getProfileImageUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, id);
                existing.setProfileImageUrl(profileImageUrl);
            }
            
            // Handle panorama image
            if (panoramaImage != null && !panoramaImage.isEmpty() && panoramaImage.getSize() > 0) {
                if (existing.getPanoramaImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getPanoramaImageUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String panoramaUrl = cloudinaryService.uploadArtisanPanoramaImage(panoramaImage, id);
                existing.setPanoramaImageUrl(panoramaUrl);
            }
            
            // Handle gallery images
            if (images != null && images.length > 0) {
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
                    List<String> imageUrls = cloudinaryService.uploadArtisanImages(validImagesArray, id);
                    String imagesJson = String.join(",", imageUrls);
                    existing.setImages(imagesJson);
                }
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
