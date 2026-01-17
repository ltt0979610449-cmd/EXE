package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.ReviewResponse;
import swd.coiviet.enums.ReviewStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Booking;
import swd.coiviet.model.Review;
import swd.coiviet.service.BookingService;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ReviewService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;
    private final CloudinaryService cloudinaryService;
    private final JwtUtil jwtUtil;
    private final swd.coiviet.service.TourService tourService;

    public ReviewController(ReviewService reviewService, BookingService bookingService, 
                           CloudinaryService cloudinaryService, JwtUtil jwtUtil,
                           swd.coiviet.service.TourService tourService) {
        this.reviewService = reviewService;
        this.bookingService = bookingService;
        this.cloudinaryService = cloudinaryService;
        this.jwtUtil = jwtUtil;
        this.tourService = tourService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo review cho tour", description = "Tạo review với rating, comment và tối đa 3 ảnh")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Parameter(description = "Booking ID", required = true)
            @RequestParam @NotNull(message = "Booking ID không được để trống") Long bookingId,
            @Parameter(description = "Rating (1-5)", required = true)
            @RequestParam @NotNull(message = "Rating không được để trống") 
            @Min(value = 1, message = "Rating phải từ 1 đến 5") 
            @Max(value = 5, message = "Rating phải từ 1 đến 5") Integer rating,
            @Parameter(description = "Comment", required = false)
            @RequestParam(required = false) String comment,
            @Parameter(description = "Danh sách ảnh (tối đa 3 ảnh, có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        
        // Validate booking
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking không tồn tại"));
        
        // Check ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền review booking này");
        }
        
        // Check if booking is completed
        if (booking.getStatus() != swd.coiviet.enums.BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể review tour đã hoàn thành");
        }
        
        // Check if already reviewed
        reviewService.findByBookingId(bookingId).ifPresent(r -> {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Bạn đã review tour này rồi");
        });
        
        // Validate images (max 3)
        if (images != null && images.length > 3) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ được upload tối đa 3 ảnh");
        }
        
        // Upload images if provided
        String imagesJson = null;
        if (images != null && images.length > 0) {
            // Filter out empty files
            List<MultipartFile> validImages = Arrays.stream(images)
                    .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                    .collect(java.util.stream.Collectors.toList());
            
            if (!validImages.isEmpty()) {
                MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                List<String> imageUrls = cloudinaryService.uploadMultipleImagesAsync(
                        validImagesArray, 
                        "reviews/" + bookingId, 
                        "raw"
                );
                imagesJson = String.join(",", imageUrls);
            }
        }
        
        // Create review
        Review review = Review.builder()
                .booking(booking)
                .user(booking.getUser())
                .tour(booking.getTour())
                .rating(rating)
                .comment(comment)
                .images(imagesJson)
                .status(ReviewStatus.VISIBLE)
                .createdAt(LocalDateTime.now())
                .build();
        
        review = reviewService.save(review);
        
        // Update tour average rating
        updateTourRating(booking.getTour().getId());
        
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(review), "Tạo review thành công"));
    }

    @GetMapping("/tour/{tourId}")
    @Operation(summary = "Lấy danh sách review của tour", description = "Lấy tất cả review đã được duyệt của tour")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByTour(@PathVariable Long tourId) {
        List<Review> reviews = reviewService.findByTourIdAndStatus(tourId, ReviewStatus.VISIBLE);
        List<ReviewResponse> responses = reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/my-reviews")
    @Operation(summary = "Lấy danh sách review của user", description = "Lấy tất cả review của user hiện tại")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<Review> reviews = reviewService.findByUserId(userId);
        List<ReviewResponse> responses = reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết review", description = "Lấy thông tin chi tiết của một review")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable Long id) {
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Review không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(review)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa review", description = "Xóa review của user")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Review không tồn tại"));
        
        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xóa review này");
        }
        
        // Delete images from Cloudinary
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            String[] imageUrls = review.getImages().split(",");
            for (String url : imageUrls) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(url.trim());
                if (publicId != null) {
                    cloudinaryService.deleteResource(publicId);
                }
            }
        }
        
        reviewService.deleteById(id);
        
        // Update tour rating
        updateTourRating(review.getTour().getId());
        
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa review thành công"));
    }

    private void updateTourRating(Long tourId) {
        tourService.updateTourRating(tourId);
    }

    private ReviewResponse mapToResponse(Review review) {
        List<String> imageList = null;
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            imageList = Arrays.asList(review.getImages().split(","));
        }
        
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking() != null ? review.getBooking().getId() : null)
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName() != null ? review.getUser().getFullName() : review.getUser().getUsername())
                .userAvatar(review.getUser().getAvatarUrl())
                .tourId(review.getTour().getId())
                .tourTitle(review.getTour().getTitle())
                .rating(review.getRating())
                .comment(review.getComment())
                .images(imageList)
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
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
