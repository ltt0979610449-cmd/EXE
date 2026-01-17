package swd.coiviet.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.ArtisanDashboardResponse;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.enums.PaymentStatus;
import swd.coiviet.enums.ReviewStatus;
import swd.coiviet.enums.Status;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.Booking;
import swd.coiviet.model.Review;
import swd.coiviet.repository.*;
import swd.coiviet.service.ArtisanService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/artisans/{artisanId}/dashboard")
public class ArtisanDashboardController {
    private final ArtisanService artisanService;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    public ArtisanDashboardController(ArtisanService artisanService,
                                     TourRepository tourRepository,
                                     BookingRepository bookingRepository,
                                     PaymentRepository paymentRepository,
                                     ReviewRepository reviewRepository) {
        this.artisanService = artisanService;
        this.tourRepository = tourRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ArtisanDashboardResponse>> getDashboard(
            @PathVariable Long artisanId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        Artisan artisan = artisanService.findById(artisanId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Artisan không tồn tại"));

        LocalDate toDate = to != null ? to : LocalDate.now();
        LocalDate fromDate = from != null ? from : toDate.minusDays(30);

        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(LocalTime.MAX);

        ArtisanDashboardResponse response = ArtisanDashboardResponse.builder()
                .artisanId(artisan.getId())
                .artisanName(artisan.getFullName())
                .fromDate(fromDate)
                .toDate(toDate)
                .tours(buildTourStats(artisanId))
                .bookings(buildBookingStats(artisanId, start, end))
                .revenue(buildRevenueStats(artisanId, start, end))
                .ratings(buildRatingStats(artisanId))
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy dashboard thành công"));
    }

    private ArtisanDashboardResponse.TourStats buildTourStats(Long artisanId) {
        List<swd.coiviet.model.Tour> tours = tourRepository.findByArtisanId(artisanId);
        long activeTours = tours.stream()
                .filter(t -> t.getStatus() == Status.ACTIVE)
                .count();

        return ArtisanDashboardResponse.TourStats.builder()
                .total(tours.size())
                .active(activeTours)
                .build();
    }

    private ArtisanDashboardResponse.BookingStats buildBookingStats(Long artisanId, LocalDateTime start, LocalDateTime end) {
        long total = bookingRepository.countByArtisanId(artisanId);
        long newInRange = bookingRepository.countByArtisanIdAndCreatedAtBetween(artisanId, start, end);
        
        List<Booking> bookings = bookingRepository.findByArtisanId(artisanId);
        long completed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();
        long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        return ArtisanDashboardResponse.BookingStats.builder()
                .total(total)
                .newInRange(newInRange)
                .completed(completed)
                .cancelled(cancelled)
                .build();
    }

    private ArtisanDashboardResponse.RevenueStats buildRevenueStats(Long artisanId, LocalDateTime start, LocalDateTime end) {
        BigDecimal totalRevenue = safeMoney(paymentRepository.sumAmountByArtisanIdAndStatus(artisanId, PaymentStatus.PAID));
        BigDecimal revenueInRange = safeMoney(paymentRepository.sumAmountByArtisanIdAndStatusAndPaidAtBetween(
                artisanId, PaymentStatus.PAID, start, end));

        List<Booking> bookings = bookingRepository.findByArtisanId(artisanId);
        long completedBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();
        
        BigDecimal averageRevenuePerBooking = completedBookings > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(completedBookings), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ArtisanDashboardResponse.RevenueStats.builder()
                .totalRevenue(totalRevenue)
                .revenueInRange(revenueInRange)
                .averageRevenuePerBooking(averageRevenuePerBooking)
                .build();
    }

    private ArtisanDashboardResponse.RatingStats buildRatingStats(Long artisanId) {
        List<swd.coiviet.model.Tour> tours = tourRepository.findByArtisanId(artisanId);
        List<Long> tourIds = tours.stream().map(swd.coiviet.model.Tour::getId).collect(Collectors.toList());

        if (tourIds.isEmpty()) {
            return ArtisanDashboardResponse.RatingStats.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .fiveStar(0)
                    .fourStar(0)
                    .threeStar(0)
                    .twoStar(0)
                    .oneStar(0)
                    .build();
        }

        List<Review> reviews = reviewRepository.findAll().stream()
                .filter(r -> tourIds.contains(r.getTour().getId()) && r.getStatus() == ReviewStatus.VISIBLE)
                .collect(Collectors.toList());

        if (reviews.isEmpty()) {
            return ArtisanDashboardResponse.RatingStats.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .fiveStar(0)
                    .fourStar(0)
                    .threeStar(0)
                    .twoStar(0)
                    .oneStar(0)
                    .build();
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<Integer, Long> ratingCounts = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        return ArtisanDashboardResponse.RatingStats.builder()
                .averageRating(averageRating)
                .totalReviews(reviews.size())
                .fiveStar(ratingCounts.getOrDefault(5, 0L).intValue())
                .fourStar(ratingCounts.getOrDefault(4, 0L).intValue())
                .threeStar(ratingCounts.getOrDefault(3, 0L).intValue())
                .twoStar(ratingCounts.getOrDefault(2, 0L).intValue())
                .oneStar(ratingCounts.getOrDefault(1, 0L).intValue())
                .build();
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
