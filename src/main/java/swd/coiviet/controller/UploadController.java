package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.UploadResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.service.CloudinaryService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Upload user avatar
     */
    @PutMapping(value = "/user/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload user avatar", description = "Cập nhật avatar từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadUserAvatar(
            @Parameter(description = "Avatar file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId) {
        try {
            String url = cloudinaryService.uploadUserAvatar(file, userId);
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .message("Upload avatar thành công")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload avatar: " + e.getMessage());
        }
    }

    /**
     * Upload tour thumbnail
     */
    @PutMapping(value = "/tour/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload tour thumbnail", description = "Cập nhật thumbnail tour từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadTourThumbnail(
            @Parameter(description = "Thumbnail file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "tourId", required = false) Long tourId) {
        try {
            String url = cloudinaryService.uploadTourThumbnail(file, tourId);
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .message("Upload thumbnail tour thành công")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload thumbnail: " + e.getMessage());
        }
    }

    /**
     * Upload multiple tour images
     */
    @PutMapping(value = "/tour/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload tour images", description = "Cập nhật nhiều ảnh tour từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadTourImages(
            @Parameter(description = "Danh sách ảnh", schema = @Schema(type = "array", format = "binary"))
            @RequestPart("files") MultipartFile[] files,
            @RequestParam(value = "tourId", required = false) Long tourId) {
        if (files == null || files.length == 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Vui lòng chọn ít nhất một ảnh");
        }
        List<String> urls = cloudinaryService.uploadTourImages(files, tourId);
        UploadResponse response = UploadResponse.builder()
                .urls(urls)
                .message("Upload " + urls.size() + " ảnh thành công")
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Upload province thumbnail
     */
    @PutMapping(value = "/province/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload province thumbnail", description = "Cập nhật thumbnail tỉnh thành từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadProvinceThumbnail(
            @Parameter(description = "Thumbnail file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "provinceId", required = false) Long provinceId) {
        try {
            String url = cloudinaryService.uploadProvinceThumbnail(file, provinceId);
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .message("Upload thumbnail tỉnh thành thành công")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload thumbnail: " + e.getMessage());
        }
    }

    /**
     * Upload culture item thumbnail
     */
    @PutMapping(value = "/culture-item/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload culture item thumbnail", description = "Cập nhật thumbnail văn hóa từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadCultureItemThumbnail(
            @Parameter(description = "Thumbnail file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "cultureItemId", required = false) Long cultureItemId) {
        try {
            String url = cloudinaryService.uploadCultureItemThumbnail(file, cultureItemId);
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .message("Upload thumbnail văn hóa thành công")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload thumbnail: " + e.getMessage());
        }
    }

    /**
     * Upload multiple culture item images
     */
    @PutMapping(value = "/culture-item/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload culture item images", description = "Cập nhật nhiều ảnh văn hóa từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadCultureItemImages(
            @Parameter(description = "Danh sách ảnh", schema = @Schema(type = "array", format = "binary"))
            @RequestPart("files") MultipartFile[] files,
            @RequestParam(value = "cultureItemId", required = false) Long cultureItemId) {
        if (files == null || files.length == 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Vui lòng chọn ít nhất một ảnh");
        }
        List<String> urls = cloudinaryService.uploadCultureItemImages(files, cultureItemId);
        UploadResponse response = UploadResponse.builder()
                .urls(urls)
                .message("Upload " + urls.size() + " ảnh thành công")
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Upload video thumbnail
     */
    @PutMapping(value = "/video/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload video thumbnail", description = "Cập nhật thumbnail video từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadVideoThumbnail(
            @Parameter(description = "Thumbnail file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "videoId", required = false) Long videoId) {
        try {
            String url = cloudinaryService.uploadVideoThumbnail(file, videoId);
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .message("Upload thumbnail video thành công")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload thumbnail: " + e.getMessage());
        }
    }

    /**
     * Upload artisan profile image
     */
    @PutMapping(value = "/artisan/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload artisan profile image", description = "Cập nhật ảnh nghệ nhân từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadArtisanProfileImage(
            @Parameter(description = "Profile image file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "artisanId", required = false) Long artisanId) {
        try {
            String url = cloudinaryService.uploadArtisanProfileImage(file, artisanId);
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .message("Upload ảnh nghệ nhân thành công")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    /**
     * Upload multiple artisan images
     */
    @PutMapping(value = "/artisan/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload artisan images", description = "Cập nhật nhiều ảnh nghệ nhân từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadArtisanImages(
            @Parameter(description = "Danh sách ảnh", schema = @Schema(type = "array", format = "binary"))
            @RequestPart("files") MultipartFile[] files,
            @RequestParam(value = "artisanId", required = false) Long artisanId) {
        if (files == null || files.length == 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Vui lòng chọn ít nhất một ảnh");
        }
        List<String> urls = cloudinaryService.uploadArtisanImages(files, artisanId);
        UploadResponse response = UploadResponse.builder()
                .urls(urls)
                .message("Upload " + urls.size() + " ảnh thành công")
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Upload blog featured image
     */
    @PutMapping(value = "/blog/featured-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload blog featured image", description = "Cập nhật ảnh blog từ file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadBlogFeaturedImage(
            @Parameter(description = "Featured image file", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "postId", required = false) Long postId) {
        try {
            String url = cloudinaryService.uploadBlogImage(file, postId);
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .message("Upload ảnh blog thành công")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    /**
     * Delete image from Cloudinary
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam("url") String url) {
        String publicId = cloudinaryService.extractPublicIdFromUrl(url);
        if (publicId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "URL không hợp lệ");
        }
        boolean deleted = cloudinaryService.deleteResource(publicId);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null, "Xóa ảnh thành công"));
        } else {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Không thể xóa ảnh");
        }
    }
}
