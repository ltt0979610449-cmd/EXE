package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Province;
import swd.coiviet.model.User;
import swd.coiviet.model.UserMemory;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.UserMemoryService;
import swd.coiviet.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/user-memories")
public class UserMemoryController {

    private final UserMemoryService userMemoryService;
    private final UserService userService;
    private final ProvinceService provinceService;
    private final CloudinaryService cloudinaryService;
    private final JwtUtil jwtUtil;

    public UserMemoryController(UserMemoryService userMemoryService, UserService userService,
                               ProvinceService provinceService, CloudinaryService cloudinaryService,
                               JwtUtil jwtUtil) {
        this.userMemoryService = userMemoryService;
        this.userService = userService;
        this.provinceService = provinceService;
        this.cloudinaryService = cloudinaryService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/public")
    @Operation(summary = "Lấy danh sách ký ức đã publish", description = "Lấy tất cả ký ức đã được publish")
    public ResponseEntity<ApiResponse<List<UserMemory>>> getPublishedMemories() {
        List<UserMemory> memories = userMemoryService.findByStatus(PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(memories));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Lấy ký ức theo ID", description = "Lấy thông tin chi tiết ký ức")
    public ResponseEntity<ApiResponse<UserMemory>> getMemoryById(@PathVariable Long id) {
        UserMemory memory = userMemoryService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Ký ức không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(memory));
    }

    @GetMapping("/public/province/{provinceId}")
    @Operation(summary = "Lấy ký ức theo tỉnh thành", description = "Lấy danh sách ký ức của một tỉnh thành")
    public ResponseEntity<ApiResponse<List<UserMemory>>> getMemoriesByProvince(@PathVariable Long provinceId) {
        List<UserMemory> memories = userMemoryService.findByProvinceIdAndStatus(provinceId, PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(memories));
    }

    @GetMapping("/my-memories")
    @Operation(summary = "Lấy ký ức của user", description = "Lấy tất cả ký ức của user hiện tại")
    public ResponseEntity<ApiResponse<List<UserMemory>>> getMyMemories(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<UserMemory> memories = userMemoryService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(memories));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo ký ức mới", description = "Tạo ký ức với hình ảnh, audio, video")
    public ResponseEntity<ApiResponse<UserMemory>> createMemory(
            @Parameter(description = "Tiêu đề ký ức", required = true)
            @RequestParam @NotBlank(message = "Tiêu đề không được để trống") String title,
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Danh sách ảnh (có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @Parameter(description = "Audio file (giọng nói địa phương)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @Parameter(description = "Video file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "video", required = false) MultipartFile video,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        User user = userService.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User không tồn tại"));
        
        try {
            UserMemory memory = UserMemory.builder()
                    .user(user)
                    .title(title)
                    .description(description)
                    .status(PublicationStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                memory.setProvince(province);
            }
            
            UserMemory saved = userMemoryService.save(memory);
            
            // Upload images if provided
            if (images != null && images.length > 0) {
                // Filter out empty files
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .collect(java.util.stream.Collectors.toList());
                
                if (!validImages.isEmpty()) {
                    MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                    List<String> imageUrls = cloudinaryService.uploadMultipleImagesAsync(
                            validImagesArray, 
                            "memories/" + saved.getId() + "/images", 
                            "raw"
                    );
                    String imagesJson = String.join(",", imageUrls);
                    saved.setImages(imagesJson);
                }
            }
            
            // Upload audio if provided
            if (audio != null && !audio.isEmpty() && audio.getSize() > 0) {
                String audioUrl = cloudinaryService.uploadAudio(audio, "memories/" + saved.getId() + "/audio");
                saved.setAudioUrl(audioUrl);
            }
            
            // Upload video if provided
            if (video != null && !video.isEmpty() && video.getSize() > 0) {
                String videoUrl = cloudinaryService.uploadVideo(video, "memories/" + saved.getId() + "/video");
                saved.setVideoUrl(videoUrl);
            }
            
            saved = userMemoryService.save(saved);
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo ký ức thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload file: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật ký ức", description = "Cập nhật thông tin ký ức và media files")
    public ResponseEntity<ApiResponse<UserMemory>> updateMemory(
            @PathVariable Long id,
            @Parameter(description = "Tiêu đề ký ức", required = false)
            @RequestParam(required = false) String title,
            @Parameter(description = "Mô tả", required = false)
            @RequestParam(required = false) String description,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Danh sách ảnh (nếu có, có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @Parameter(description = "Audio file (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @Parameter(description = "Video file (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "video", required = false) MultipartFile video,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        UserMemory existing = userMemoryService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Ký ức không tồn tại"));
        
        // Check ownership
        if (!existing.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền cập nhật ký ức này");
        }
        
        try {
            // Update fields
            if (title != null) existing.setTitle(title);
            if (description != null) existing.setDescription(description);
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            
            // Handle images
            if (images != null && images.length > 0) {
                // Filter out empty files
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .collect(java.util.stream.Collectors.toList());
                
                if (!validImages.isEmpty()) {
                    // Delete old images
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
                    List<String> imageUrls = cloudinaryService.uploadMultipleImagesAsync(
                            validImagesArray, 
                            "memories/" + id + "/images", 
                            "raw"
                    );
                    existing.setImages(String.join(",", imageUrls));
                }
            }
            
            // Handle audio
            if (audio != null && !audio.isEmpty() && audio.getSize() > 0) {
                if (existing.getAudioUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getAudioUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId, "video");
                    }
                }
                String audioUrl = cloudinaryService.uploadAudio(audio, "memories/" + id + "/audio");
                existing.setAudioUrl(audioUrl);
            }
            
            // Handle video
            if (video != null && !video.isEmpty() && video.getSize() > 0) {
                if (existing.getVideoUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getVideoUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId, "video");
                    }
                }
                String videoUrl = cloudinaryService.uploadVideo(video, "memories/" + id + "/video");
                existing.setVideoUrl(videoUrl);
            }
            
            existing.setUpdatedAt(LocalDateTime.now());
            UserMemory updated = userMemoryService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật ký ức thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload file: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish ký ức", description = "Chuyển ký ức sang trạng thái published")
    public ResponseEntity<ApiResponse<UserMemory>> publishMemory(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        UserMemory memory = userMemoryService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Ký ức không tồn tại"));
        
        // Check ownership
        if (!memory.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền publish ký ức này");
        }
        
        memory.setStatus(PublicationStatus.PUBLISHED);
        memory.setUpdatedAt(LocalDateTime.now());
        UserMemory updated = userMemoryService.save(memory);
        return ResponseEntity.ok(ApiResponse.success(updated, "Publish ký ức thành công"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa ký ức", description = "Xóa ký ức của user")
    public ResponseEntity<ApiResponse<Void>> deleteMemory(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        UserMemory memory = userMemoryService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Ký ức không tồn tại"));
        
        // Check ownership
        if (!memory.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xóa ký ức này");
        }
        
        // Delete media files from Cloudinary
        if (memory.getImages() != null && !memory.getImages().isEmpty()) {
            String[] imageUrls = memory.getImages().split(",");
            for (String url : imageUrls) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(url.trim());
                if (publicId != null) {
                    cloudinaryService.deleteResource(publicId);
                }
            }
        }
        if (memory.getAudioUrl() != null) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(memory.getAudioUrl());
            if (publicId != null) {
                cloudinaryService.deleteResource(publicId, "video");
            }
        }
        if (memory.getVideoUrl() != null) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(memory.getVideoUrl());
            if (publicId != null) {
                cloudinaryService.deleteResource(publicId, "video");
            }
        }
        
        userMemoryService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa ký ức thành công"));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ");
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn");
            }
            
            io.jsonwebtoken.Claims claims = jwtUtil.getClaims(token);
            Integer userId = claims.get("userId", Integer.class);
            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không chứa thông tin user");
            }
            return Long.valueOf(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
        }
    }
}
