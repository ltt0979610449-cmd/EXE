package swd.coiviet.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    @Autowired
    private Cloudinary cloudinary;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private static final Map<String, String> PRESETS = Map.of(
            "user", "w_300,h_300,c_fill,g_face,q_auto,f_jpg",
            "artisan", "w_800,c_scale,q_auto,f_jpg",
            "blog", "w_1060,c_scale,q_auto,f_jpg",
            "video", "w_1280,c_scale,q_auto,f_jpg",
            "tour", "w_1200,c_scale,q_auto,f_jpg",
            "tour-thumbnail", "w_600,h_400,c_fill,q_auto,f_jpg",
            "province", "w_1000,c_scale,q_auto,f_jpg",
            "culture", "w_1000,c_scale,q_auto,f_jpg",
            "raw", "q_auto,f_auto"
    );

    public String uploadImage(MultipartFile file, String folder, String preset) throws IOException {
        validateFile(file);
        String transformation = PRESETS.getOrDefault(preset, PRESETS.get("raw"));
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "transformation", transformation,
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String url = Objects.toString(uploadResult.get("secure_url"), null);
            logger.info("Image uploaded successfully: {}", url);
            return url;
        } catch (Exception e) {
            logger.error("Failed to upload image: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload image: " + e.getMessage());
        }
    }

    public String uploadRawImage(MultipartFile file, String folder) throws IOException {
        validateFile(file);
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "quality", "auto",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String url = Objects.toString(uploadResult.get("secure_url"), null);
            logger.info("Raw image uploaded successfully: {}", url);
            return url;
        } catch (Exception e) {
            logger.error("Failed to upload raw image: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload raw image: " + e.getMessage());
        }
    }

    public List<String> uploadMultipleImagesAsync(MultipartFile[] files, String folder, String preset) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (MultipartFile file : files) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadImage(file, folder, preset);
                } catch (IOException e) {
                    logger.error("Failed to upload file: {}", file == null ? "<null>" : file.getOriginalFilename(), e);
                    return null;
                }
            }, executorService);
            futures.add(future);
        }
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }

    public String uploadUserAvatar(MultipartFile file, Long userId) throws IOException {
        String folder = String.format("users/%d/avatar", userId == null ? 0 : userId);
        return uploadImage(file, folder, "user");
    }

    public List<String> uploadArtisanImages(MultipartFile[] files, Long artisanId) {
        String folder = String.format("artisans/%d/images", artisanId == null ? 0 : artisanId);
        return uploadMultipleImagesAsync(files, folder, "artisan");
    }

    public String uploadBlogImage(MultipartFile file, Long postId) throws IOException {
        String folder = String.format("blogs/%d/images", postId == null ? 0 : postId);
        return uploadImage(file, folder, "blog");
    }

    public String uploadTourThumbnail(MultipartFile file, Long tourId) throws IOException {
        String folder = String.format("tours/%d/thumbnail", tourId == null ? 0 : tourId);
        return uploadImage(file, folder, "tour-thumbnail");
    }

    public List<String> uploadTourImages(MultipartFile[] files, Long tourId) {
        String folder = String.format("tours/%d/images", tourId == null ? 0 : tourId);
        return uploadMultipleImagesAsync(files, folder, "tour");
    }

    public String uploadProvinceThumbnail(MultipartFile file, Long provinceId) throws IOException {
        String folder = String.format("provinces/%d/thumbnail", provinceId == null ? 0 : provinceId);
        return uploadImage(file, folder, "province");
    }

    public String uploadCultureItemThumbnail(MultipartFile file, Long cultureItemId) throws IOException {
        String folder = String.format("culture-items/%d/thumbnail", cultureItemId == null ? 0 : cultureItemId);
        return uploadImage(file, folder, "culture");
    }

    public List<String> uploadCultureItemImages(MultipartFile[] files, Long cultureItemId) {
        String folder = String.format("culture-items/%d/images", cultureItemId == null ? 0 : cultureItemId);
        return uploadMultipleImagesAsync(files, folder, "culture");
    }

    public String uploadVideoThumbnail(MultipartFile file, Long videoId) throws IOException {
        String folder = String.format("videos/%d/thumbnail", videoId == null ? 0 : videoId);
        return uploadImage(file, folder, "video");
    }

    public String uploadArtisanProfileImage(MultipartFile file, Long artisanId) throws IOException {
        String folder = String.format("artisans/%d/profile", artisanId == null ? 0 : artisanId);
        return uploadImage(file, folder, "artisan");
    }

    public boolean deleteResource(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            boolean success = "ok".equals(result.get("result"));
            if (success) {
                logger.info("Resource deleted successfully: {}", publicId);
            } else {
                logger.warn("Failed to delete resource: {}", publicId);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error deleting resource: {}", publicId, e);
            return false;
        }
    }

    public boolean deleteResource(String publicId, String resourceType) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
            boolean success = "ok".equals(result.get("result"));
            if (success) {
                logger.info("Resource deleted successfully: {} (type: {})", publicId, resourceType);
            } else {
                logger.warn("Failed to delete resource: {} (type: {})", publicId, resourceType);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error deleting resource: {} (type: {})", publicId, resourceType, e);
            return false;
        }
    }

    public String extractPublicIdFromUrl(String url) {
        try {
            if (url == null || !url.contains("cloudinary.com")) {
                return null;
            }
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;
            String afterUpload = url.substring(uploadIndex + 8);
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                int slashIndex = afterUpload.indexOf('/');
                afterUpload = afterUpload.substring(slashIndex + 1);
            }
            int dotIndex = afterUpload.lastIndexOf('.');
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            return afterUpload;
        } catch (Exception e) {
            logger.error("Error extracting public ID from URL: {}", url, e);
            return null;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "File cannot be null or empty");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "File size too large. Maximum 10MB allowed");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid file type. Only images are allowed");
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
