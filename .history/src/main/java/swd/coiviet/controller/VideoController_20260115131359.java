package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.CreateVideoRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Province;
import swd.coiviet.model.Video;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.CultureItemService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.VideoService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;
    private final CloudinaryService cloudinaryService;
    private final ProvinceService provinceService;
    private final CultureItemService cultureItemService;

    public VideoController(VideoService videoService, CloudinaryService cloudinaryService,
                          ProvinceService provinceService, CultureItemService cultureItemService) {
        this.videoService = videoService;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
        this.cultureItemService = cultureItemService;
    }

    @GetMapping("/public")
    @Operation(summary = "Lấy danh sách videos đã publish", description = "Lấy tất cả videos đã được publish")
    public ResponseEntity<ApiResponse<List<Video>>> getPublishedVideos() {
        List<Video> videos = videoService.findByStatus(PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(videos));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Lấy video theo ID", description = "Lấy thông tin chi tiết video")
    public ResponseEntity<ApiResponse<Video>> getVideoById(@PathVariable Long id) {
        Video video = videoService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Video không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(video));
    }

    @GetMapping("/public/province/{provinceId}")
    @Operation(summary = "Lấy videos theo tỉnh thành", description = "Lấy danh sách videos của một tỉnh thành")
    public ResponseEntity<ApiResponse<List<Video>>> getVideosByProvince(@PathVariable Long provinceId) {
        List<Video> videos = videoService.findByProvinceId(provinceId);
        return ResponseEntity.ok(ApiResponse.success(videos));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo video mới", description = "Tạo video với thumbnail")
    public ResponseEntity<ApiResponse<Video>> createVideo(
            @ModelAttribute @Validated CreateVideoRequest request,
            @Parameter(description = "Thumbnail image", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        try {
            Video video = Video.builder()
                    .title(request.getTitle())
                    .videoUrl(request.getVideoUrl())
                    .status(PublicationStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                video.setProvince(province);
            }
            
            if (request.getCultureItemId() != null) {
                CultureItem cultureItem = cultureItemService.findById(request.getCultureItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn hóa không tồn tại"));
                video.setCultureItem(cultureItem);
            }
            
            Video saved = videoService.save(video);
            
            // Upload thumbnail if provided
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailUrl = cloudinaryService.uploadVideoThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(thumbnailUrl);
                saved = videoService.save(saved);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo video thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật video", description = "Cập nhật thông tin video và thumbnail")
    public ResponseEntity<ApiResponse<Video>> updateVideo(
            @PathVariable Long id,
            @ModelAttribute CreateVideoRequest request,
            @Parameter(description = "Thumbnail mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Video existing = videoService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Video không tồn tại"));
        
        try {
            // Update fields
            if (request.getTitle() != null) existing.setTitle(request.getTitle());
            if (request.getVideoUrl() != null) existing.setVideoUrl(request.getVideoUrl());
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            if (request.getCultureItemId() != null) {
                CultureItem cultureItem = cultureItemService.findById(request.getCultureItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn hóa không tồn tại"));
                existing.setCultureItem(cultureItem);
            }
            
            // Handle thumbnail
            if (thumbnail != null && !thumbnail.isEmpty()) {
                if (existing.getThumbnailUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getThumbnailUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String thumbnailUrl = cloudinaryService.uploadVideoThumbnail(thumbnail, id);
                existing.setThumbnailUrl(thumbnailUrl);
            }
            
            Video updated = videoService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật video thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish video", description = "Chuyển video sang trạng thái published")
    public ResponseEntity<ApiResponse<Video>> publishVideo(@PathVariable Long id) {
        Video video = videoService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Video không tồn tại"));
        video.setStatus(PublicationStatus.PUBLISHED);
        Video updated = videoService.save(video);
        return ResponseEntity.ok(ApiResponse.success(updated, "Publish video thành công"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa video", description = "Xóa video")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Long id) {
        Video video = videoService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Video không tồn tại"));
        
        // Delete thumbnail
        if (video.getThumbnailUrl() != null) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(video.getThumbnailUrl());
            if (publicId != null) {
                cloudinaryService.deleteResource(publicId);
            }
        }
        
        videoService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa video thành công"));
    }
}
