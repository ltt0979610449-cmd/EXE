package swd.coiviet.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.ReviewResponse;
import swd.coiviet.enums.ReviewStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Review;
import swd.coiviet.service.ReviewService;
import swd.coiviet.service.TourService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
@Tag(name = "Admin", description = "Quản lý review - duyệt/từ chối")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final TourService tourService;

    public AdminReviewController(ReviewService reviewService, TourService tourService) {
        this.reviewService = reviewService;
        this.tourService = tourService;
    }

    @GetMapping
    @Operation(summary = "Danh sách review", description = "Lấy tất cả review, có thể filter theo status")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> listReviews(
            @Parameter(description = "Trạng thái: VISIBLE, HIDDEN, FLAGGED")
            @RequestParam(required = false) ReviewStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Review> reviews = status != null
                ? reviewService.findByStatus(status, pageable)
                : reviewService.findAll(pageable);
        Page<ReviewResponse> response = reviews.map(this::mapToResponse);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách review thành công"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Duyệt/Từ chối review", description = "Cập nhật trạng thái review: VISIBLE (duyệt), HIDDEN (ẩn/từ chối)")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReviewStatus(
            @PathVariable Long id,
            @Parameter(description = "Trạng thái mới: VISIBLE, HIDDEN, FLAGGED", required = true)
            @RequestParam ReviewStatus status) {
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Review không tồn tại"));
        if (!Arrays.asList(ReviewStatus.VISIBLE, ReviewStatus.HIDDEN, ReviewStatus.FLAGGED).contains(status)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Trạng thái không hợp lệ");
        }
        review.setStatus(status);
        review = reviewService.save(review);
        tourService.updateTourRating(review.getTour().getId());
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(review), "Cập nhật trạng thái review thành công"));
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
}
