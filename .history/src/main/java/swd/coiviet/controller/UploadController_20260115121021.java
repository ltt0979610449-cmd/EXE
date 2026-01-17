package swd.coiviet.controller;

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
    @PostMapping("/user/avatar")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadUserAvatar(
            @RequestParam("file") MultipartFile file,
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
    @PostMapping("/tour/thumbnail")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadTourThumbnail(
            @RequestParam("file") MultipartFile file,
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
    @PostMapping("/tour/images")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadTourImages(
            @RequestParam("files") MultipartFile[] files,
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
    @PostMapping("/province/thumbnail")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadProvinceThumbnail(
            @RequestParam("file") MultipartFile file,
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
    @PostMapping("/culture-item/thumbnail")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadCultureItemThumbnail(
            @RequestParam("file") MultipartFile file,
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
    @PostMapping("/culture-item/images")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadCultureItemImages(
            @RequestParam("files") MultipartFile[] files,
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
    @PostMapping("/video/thumbnail")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadVideoThumbnail(
            @RequestParam("file") MultipartFile file,
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
    @PostMapping("/artisan/profile")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadArtisanProfileImage(
            @RequestParam("file") MultipartFile file,
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
    @PostMapping("/artisan/images")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadArtisanImages(
            @RequestParam("files") MultipartFile[] files,
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
    @PostMapping("/blog/featured-image")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadBlogFeaturedImage(
            @RequestParam("file") MultipartFile file,
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
