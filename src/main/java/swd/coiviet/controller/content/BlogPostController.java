package swd.coiviet.controller.content;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.BlogPostDetailResponse;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.BlogPost;
import swd.coiviet.model.Province;
import swd.coiviet.service.BlogPostService;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/blog-posts")
@Tag(name = "Content", description = "Bài viết blog")
public class BlogPostController {

    private final BlogPostService blogPostService;
    private final CloudinaryService cloudinaryService;
    private final ProvinceService provinceService;

    public BlogPostController(BlogPostService blogPostService, CloudinaryService cloudinaryService, ProvinceService provinceService) {
        this.blogPostService = blogPostService;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
    }

    @GetMapping("/public")
    @Operation(summary = "Lấy danh sách blog posts đã publish", description = "Lấy tất cả blog posts đã được publish")
    public ResponseEntity<ApiResponse<List<BlogPost>>> getPublishedBlogPosts() {
        List<BlogPost> posts = blogPostService.findAll().stream()
                .filter(p -> p.getStatus() == PublicationStatus.PUBLISHED)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Lấy blog post theo ID", description = "Lấy thông tin chi tiết blog post")
    public ResponseEntity<ApiResponse<BlogPost>> getBlogPostById(@PathVariable Long id) {
        BlogPost post = blogPostService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @GetMapping("/public/slug/{slug}")
    @Operation(summary = "Lấy blog post theo slug", description = "Lấy thông tin blog post bằng slug")
    public ResponseEntity<ApiResponse<BlogPost>> getBlogPostBySlug(@PathVariable String slug) {
        BlogPost post = blogPostService.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @GetMapping("/public/{id}/detail")
    @Operation(summary = "Lấy chi tiết blog post (format artisan)", description = "Trả về thông tin đầy đủ cho FE: heroSubtitle, narrativeContent, images parsed")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> getBlogPostDetail(@PathVariable Long id) {
        BlogPostDetailResponse detail = blogPostService.getDetailById(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/public/slug/{slug}/detail")
    @Operation(summary = "Lấy chi tiết blog post theo slug (format artisan)", description = "Trả về thông tin đầy đủ cho FE theo slug")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> getBlogPostDetailBySlug(@PathVariable String slug) {
        BlogPostDetailResponse detail = blogPostService.getDetailBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo blog post mới", description = "Tạo blog post với featured image (format giống artisan)")
    public ResponseEntity<ApiResponse<BlogPost>> createBlogPost(
            @Parameter(description = "Tiêu đề blog post", required = true)
            @RequestParam @NotBlank(message = "Tiêu đề không được để trống") String title,
            @Parameter(description = "Slug của blog post", required = false)
            @RequestParam(required = false) String slug,
            @Parameter(description = "Nội dung intro", required = false)
            @RequestParam(required = false) String content,
            @Parameter(description = "Mô tả ngắn cho hero", required = false)
            @RequestParam(required = false) String heroSubtitle,
            @Parameter(description = "Narrative JSON: [{\"title\":\"...\",\"content\":\"...\",\"imageUrl\":\"...\"}]", required = false)
            @RequestParam(required = false) String narrativeContent,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Featured image", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
            @Parameter(description = "Ảnh panorama full-width", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "panoramaImage", required = false) MultipartFile panoramaImage,
            @Parameter(description = "Danh sách ảnh gallery (có thể chọn nhiều ảnh)",
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            boolean contentBlank = isBlank(content);
            boolean narrativeBlank = isBlank(narrativeContent);
            if (contentBlank && narrativeBlank) {
                throw new AppException(ErrorCode.REQUIRED_FIELD_MISSING, "Nội dung hoặc narrativeContent không được để trống");
            }
            BlogPost post = BlogPost.builder()
                    .title(title)
                    .slug(slug)
                    .content(contentBlank ? "" : content)
                    .heroSubtitle(heroSubtitle)
                    .narrativeContent(narrativeContent)
                    .status(PublicationStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                post.setProvince(province);
            }
            
            BlogPost saved = blogPostService.save(post);
            
            // Upload featured image if provided
            if (featuredImage != null && !featuredImage.isEmpty() && featuredImage.getSize() > 0) {
                String featuredImageUrl = cloudinaryService.uploadBlogImage(featuredImage, saved.getId());
                saved.setFeaturedImageUrl(featuredImageUrl);
                saved = blogPostService.save(saved);
            }

            // Upload panorama image if provided
            if (panoramaImage != null && !panoramaImage.isEmpty() && panoramaImage.getSize() > 0) {
                String panoramaImageUrl = cloudinaryService.uploadBlogImage(panoramaImage, saved.getId());
                saved.setPanoramaImageUrl(panoramaImageUrl);
                saved = blogPostService.save(saved);
            }

            // Upload gallery images if provided
            if (images != null && images.length > 0) {
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .toList();
                if (!validImages.isEmpty()) {
                    MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                    List<String> imageUrls = cloudinaryService.uploadBlogImages(validImagesArray, saved.getId());
                    String imagesJson = String.join(",", imageUrls);
                    saved.setImages(imagesJson);
                    saved = blogPostService.save(saved);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo blog post thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật blog post", description = "Cập nhật thông tin blog post và featured image (format giống artisan)")
    public ResponseEntity<ApiResponse<BlogPost>> updateBlogPost(
            @PathVariable Long id,
            @Parameter(description = "Tiêu đề blog post", required = false)
            @RequestParam(required = false) String title,
            @Parameter(description = "Slug của blog post", required = false)
            @RequestParam(required = false) String slug,
            @Parameter(description = "Nội dung intro", required = false)
            @RequestParam(required = false) String content,
            @Parameter(description = "Mô tả ngắn cho hero", required = false)
            @RequestParam(required = false) String heroSubtitle,
            @Parameter(description = "Narrative JSON: [{\"title\":\"...\",\"content\":\"...\",\"imageUrl\":\"...\"}]", required = false)
            @RequestParam(required = false) String narrativeContent,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Featured image mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
            @Parameter(description = "Ảnh panorama full-width (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "panoramaImage", required = false) MultipartFile panoramaImage,
            @Parameter(description = "Danh sách ảnh gallery (có thể chọn nhiều ảnh)",
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        BlogPost existing = blogPostService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        
        try {
            boolean contentBlank = isBlank(content);
            boolean narrativeBlank = isBlank(narrativeContent);
            if (contentBlank && narrativeBlank && (content != null || narrativeContent != null)) {
                throw new AppException(ErrorCode.REQUIRED_FIELD_MISSING, "Nội dung hoặc narrativeContent không được để trống");
            }
            // Update fields
            if (title != null) existing.setTitle(title);
            if (slug != null) existing.setSlug(slug);
            if (content != null) existing.setContent(content);
            if (heroSubtitle != null) existing.setHeroSubtitle(heroSubtitle);
            if (narrativeContent != null) existing.setNarrativeContent(narrativeContent);
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            
            // Handle featured image
            if (featuredImage != null && !featuredImage.isEmpty() && featuredImage.getSize() > 0) {
                if (existing.getFeaturedImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getFeaturedImageUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String featuredImageUrl = cloudinaryService.uploadBlogImage(featuredImage, id);
                existing.setFeaturedImageUrl(featuredImageUrl);
            }

            // Handle panorama image
            if (panoramaImage != null && !panoramaImage.isEmpty() && panoramaImage.getSize() > 0) {
                if (existing.getPanoramaImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getPanoramaImageUrl());
                    if (publicId != null) cloudinaryService.deleteResource(publicId);
                }
                String panoramaImageUrl = cloudinaryService.uploadBlogImage(panoramaImage, id);
                existing.setPanoramaImageUrl(panoramaImageUrl);
            }

            // Handle gallery images
            if (images != null && images.length > 0) {
                List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .toList();
                if (!validImages.isEmpty()) {
                    if (existing.getImages() != null && !existing.getImages().isEmpty()) {
                        String[] oldImageUrls = existing.getImages().split(",");
                        for (String oldUrl : oldImageUrls) {
                            String publicId = cloudinaryService.extractPublicIdFromUrl(oldUrl.trim());
                            if (publicId != null) cloudinaryService.deleteResource(publicId);
                        }
                    }
                    MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                    List<String> imageUrls = cloudinaryService.uploadBlogImages(validImagesArray, id);
                    String imagesJson = String.join(",", imageUrls);
                    existing.setImages(imagesJson);
                }
            }
            
            BlogPost updated = blogPostService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật blog post thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish blog post", description = "Chuyển blog post sang trạng thái published")
    public ResponseEntity<ApiResponse<BlogPost>> publishBlogPost(@PathVariable Long id) {
        BlogPost post = blogPostService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        post.setStatus(PublicationStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        BlogPost updated = blogPostService.save(post);
        return ResponseEntity.ok(ApiResponse.success(updated, "Publish blog post thành công"));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Điều chỉnh trạng thái blog post", description = "Đặt trạng thái: DRAFT, PUBLISHED, ARCHIVED. Cần role STAFF/ADMIN.")
    public ResponseEntity<ApiResponse<BlogPost>> updateBlogPostStatus(
            @PathVariable Long id,
            @RequestParam PublicationStatus status) {
        BlogPost post = blogPostService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        post.setStatus(status);
        if (status == PublicationStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        BlogPost updated = blogPostService.save(post);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa blog post", description = "Xóa blog post")
    public ResponseEntity<ApiResponse<Void>> deleteBlogPost(@PathVariable Long id) {
        BlogPost post = blogPostService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        
        // Delete featured image
        if (post.getFeaturedImageUrl() != null) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(post.getFeaturedImageUrl());
            if (publicId != null) cloudinaryService.deleteResource(publicId);
        }
        // Delete panorama image
        if (post.getPanoramaImageUrl() != null) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(post.getPanoramaImageUrl());
            if (publicId != null) cloudinaryService.deleteResource(publicId);
        }
        // Delete gallery images
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            String[] imageUrls = post.getImages().split(",");
            for (String url : imageUrls) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(url.trim());
                if (publicId != null) cloudinaryService.deleteResource(publicId);
            }
        }
        
        blogPostService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa blog post thành công"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
