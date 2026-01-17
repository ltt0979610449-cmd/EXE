package swd.coiviet.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.request.CancelBookingRequest;
import swd.coiviet.dto.request.CreateBookingRequest;
import swd.coiviet.dto.request.SuggestTourRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.BookingResponse;
import swd.coiviet.dto.response.TourSuggestionResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Booking;
import swd.coiviet.service.BookingService;
import swd.coiviet.service.TourWorkflowService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final TourWorkflowService tourWorkflowService;
    private final JwtUtil jwtUtil;

    public BookingController(
            BookingService bookingService,
            TourWorkflowService tourWorkflowService,
            JwtUtil jwtUtil) {
        this.bookingService = bookingService;
        this.tourWorkflowService = tourWorkflowService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Tạo booking mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Validated @RequestBody CreateBookingRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        BookingResponse response = bookingService.createBooking(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đặt tour thành công"));
    }

    /**
     * Lấy danh sách booking của user hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<Booking> bookings = bookingService.findByUserId(userId);
        List<BookingResponse> responses = bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lấy chi tiết booking
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Booking booking = bookingService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking không tồn tại"));
        
        // Check ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem booking này");
        }
        
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(booking)));
    }

    /**
     * Hủy booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (request == null) {
            request = new CancelBookingRequest();
        }
        BookingResponse response = bookingService.cancelBooking(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Hủy tour thành công"));
    }

    /**
     * Gợi ý tour dựa trên vị trí (AI suggestion)
     */
    @PostMapping("/suggest")
    public ResponseEntity<ApiResponse<List<TourSuggestionResponse>>> suggestTours(
            @Validated @RequestBody SuggestTourRequest request) {
        List<TourSuggestionResponse> suggestions = tourWorkflowService.suggestTours(request);
        return ResponseEntity.ok(ApiResponse.success(suggestions, "Gợi ý tour thành công"));
    }

    /**
     * Kiểm tra tính khả dụng của tour schedule
     */
    @GetMapping("/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @RequestParam Long tourScheduleId,
            @RequestParam Integer numParticipants) {
        boolean available = tourWorkflowService.checkTourAvailability(tourScheduleId, numParticipants);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    /**
     * Tính phí hủy tour
     */
    @GetMapping("/{id}/cancellation-fee")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getCancellationFee(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Booking booking = bookingService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking không tồn tại"));
        
        // Check ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem booking này");
        }
        
        java.math.BigDecimal fee = bookingService.calculateCancellationFee(booking);
        return ResponseEntity.ok(ApiResponse.success(fee));
    }

    /**
     * Lấy userId từ JWT token
     */
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
            
            Claims claims = jwtUtil.getClaims(token);
            Integer userId = claims.get("userId", Integer.class);
            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không chứa thông tin user");
            }
            return Long.valueOf(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUser().getId())
                .tourId(booking.getTour().getId())
                .tourTitle(booking.getTour().getTitle())
                .tourScheduleId(booking.getTourSchedule().getId())
                .tourDate(booking.getTourSchedule().getTourDate() != null 
                        ? booking.getTourSchedule().getTourDate().atStartOfDay() 
                        : null)
                .tourStartTime(booking.getTourSchedule().getStartTime() != null 
                        ? booking.getTourSchedule().getTourDate().atTime(booking.getTourSchedule().getStartTime())
                        : null)
                .numParticipants(booking.getNumParticipants())
                .contactName(booking.getContactName())
                .contactPhone(booking.getContactPhone())
                .contactEmail(booking.getContactEmail())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .paidAt(booking.getPaidAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationFee(booking.getCancellationFee())
                .refundAmount(booking.getRefundAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
