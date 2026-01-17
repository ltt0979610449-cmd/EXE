package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.CreateBlogPostRequest;
import swd.coiviet.dto.request.UpdateBlogPostRequest;
import swd.coiviet.dto.response.ApiResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/blog-posts")
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

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo blog post mới", description = "Tạo blog post với featured image")
    public ResponseEntity<ApiResponse<BlogPost>> createBlogPost(
            @ModelAttribute @Validated CreateBlogPostRequest request,
            @Parameter(description = "Featured image", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        return createBlogPostInternal(request, featuredImage);
    }

    private ResponseEntity<ApiResponse<BlogPost>> createBlogPostInternal(
            CreateBlogPostRequest request,
            MultipartFile featuredImage) {
        try {
            boolean contentBlank = isBlank(request.getContent());
            boolean blocksBlank = isBlank(request.getBlocksJson());
            if (contentBlank && blocksBlank) {
                throw new AppException(ErrorCode.REQUIRED_FIELD_MISSING, "Nội dung hoặc blocks không được để trống");
            }
            BlogPost post = BlogPost.builder()
                    .title(request.getTitle())
                    .slug(request.getSlug())
                    .content(contentBlank ? "" : request.getContent())
                    .blocksJson(request.getBlocksJson())
                    .status(PublicationStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                post.setProvince(province);
            }
            
            BlogPost saved = blogPostService.save(post);
            
            // Upload featured image if provided
            if (featuredImage != null && !featuredImage.isEmpty()) {
                String featuredImageUrl = cloudinaryService.uploadBlogImage(featuredImage, saved.getId());
                saved.setFeaturedImageUrl(featuredImageUrl);
                saved = blogPostService.save(saved);
            }
            
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo blog post thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật blog post", description = "Cập nhật thông tin blog post và featured image")
    public ResponseEntity<ApiResponse<BlogPost>> updateBlogPost(
            @PathVariable Long id,
            @ModelAttribute UpdateBlogPostRequest request,
            @Parameter(description = "Featured image mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        BlogPost existing = blogPostService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        
        try {
            boolean contentBlank = isBlank(request.getContent());
            boolean blocksBlank = isBlank(request.getBlocksJson());
            if (contentBlank && blocksBlank) {
                throw new AppException(ErrorCode.REQUIRED_FIELD_MISSING, "Nội dung hoặc blocks không được để trống");
            }
            // Update fields
            if (request.getTitle() != null) existing.setTitle(request.getTitle());
            if (request.getSlug() != null) existing.setSlug(request.getSlug());
            if (request.getContent() != null) existing.setContent(request.getContent());
            if (request.getBlocksJson() != null) existing.setBlocksJson(request.getBlocksJson());
            if (request.getProvinceId() != null) {
                Province province = provinceService.findById(request.getProvinceId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            
            // Handle featured image
            if (featuredImage != null && !featuredImage.isEmpty()) {
                if (existing.getFeaturedImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getFeaturedImageUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteResource(publicId);
                    }
                }
                String featuredImageUrl = cloudinaryService.uploadBlogImage(featuredImage, id);
                existing.setFeaturedImageUrl(featuredImageUrl);
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa blog post", description = "Xóa blog post")
    public ResponseEntity<ApiResponse<Void>> deleteBlogPost(@PathVariable Long id) {
        BlogPost post = blogPostService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        
        // Delete featured image
        if (post.getFeaturedImageUrl() != null) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(post.getFeaturedImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteResource(publicId);
            }
        }
        
        blogPostService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa blog post thành công"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
