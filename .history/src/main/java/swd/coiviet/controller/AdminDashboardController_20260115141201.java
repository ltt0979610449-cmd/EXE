package swd.coiviet.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swd.coiviet.dto.response.AdminDashboardResponse;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.enums.*;
import swd.coiviet.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final BlogPostRepository blogPostRepository;
    private final VideoRepository videoRepository;
    private final CultureItemRepository cultureItemRepository;
    private final UserMemoryRepository userMemoryRepository;
    private final NotificationRepository notificationRepository;

    public AdminDashboardController(UserRepository userRepository,
                                    TourRepository tourRepository,
                                    TourScheduleRepository tourScheduleRepository,
                                    BookingRepository bookingRepository,
                                    PaymentRepository paymentRepository,
                                    ReviewRepository reviewRepository,
                                    BlogPostRepository blogPostRepository,
                                    VideoRepository videoRepository,
                                    CultureItemRepository cultureItemRepository,
                                    UserMemoryRepository userMemoryRepository,
                                    NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.blogPostRepository = blogPostRepository;
        this.videoRepository = videoRepository;
        this.cultureItemRepository = cultureItemRepository;
        this.userMemoryRepository = userMemoryRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getSummary(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate toDate = to != null ? to : LocalDate.now();
        LocalDate fromDate = from != null ? from : toDate.minusDays(30);

        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(LocalTime.MAX);

        AdminDashboardResponse response = AdminDashboardResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .users(buildUserStats(start, end))
                .tours(buildTourStats(start, end))
                .tourSchedules(buildTourScheduleStats(start, end))
                .bookings(buildBookingStats(start, end))
                .payments(buildPaymentStats(start, end))
                .reviews(buildReviewStats(start, end))
                .content(buildContentStats(start, end))
                .notifications(buildNotificationStats(start, end))
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy dashboard thành công"));
    }

    private AdminDashboardResponse.UserStats buildUserStats(LocalDateTime start, LocalDateTime end) {
        return AdminDashboardResponse.UserStats.builder()
                .total(userRepository.count())
                .newInRange(userRepository.countByCreatedAtBetween(start, end))
                .byStatus(buildCountByStatus(Status.values(), userRepository::countByStatus))
                .build();
    }

    private AdminDashboardResponse.TourStats buildTourStats(LocalDateTime start, LocalDateTime end) {
        return AdminDashboardResponse.TourStats.builder()
                .total(tourRepository.count())
                .newInRange(tourRepository.countByCreatedAtBetween(start, end))
                .byStatus(buildCountByStatus(Status.values(), tourRepository::countByStatus))
                .build();
    }

    private AdminDashboardResponse.TourScheduleStats buildTourScheduleStats(LocalDateTime start, LocalDateTime end) {
        return AdminDashboardResponse.TourScheduleStats.builder()
                .total(tourScheduleRepository.count())
                .upcoming(tourScheduleRepository.countByTourDateGreaterThanEqual(LocalDate.now()))
                .byStatus(buildCountByStatus(TourScheduleStatus.values(), tourScheduleRepository::countByStatus))
                .build();
    }

    private AdminDashboardResponse.BookingStats buildBookingStats(LocalDateTime start, LocalDateTime end) {
        return AdminDashboardResponse.BookingStats.builder()
                .total(bookingRepository.count())
                .newInRange(bookingRepository.countByCreatedAtBetween(start, end))
                .byStatus(buildCountByStatus(BookingStatus.values(), bookingRepository::countByStatus))
                .build();
    }

    private AdminDashboardResponse.PaymentStats buildPaymentStats(LocalDateTime start, LocalDateTime end) {
        BigDecimal totalRevenue = safeMoney(paymentRepository.sumAmountByStatus(PaymentStatus.PAID));
        BigDecimal revenueInRange = safeMoney(paymentRepository.sumAmountByStatusAndPaidAtBetween(PaymentStatus.PAID, start, end));

        return AdminDashboardResponse.PaymentStats.builder()
                .total(paymentRepository.count())
                .newInRange(paymentRepository.countByCreatedAtBetween(start, end))
                .byStatus(buildCountByStatus(PaymentStatus.values(), paymentRepository::countByStatus))
                .totalRevenue(totalRevenue)
                .revenueInRange(revenueInRange)
                .build();
    }

    private AdminDashboardResponse.ReviewStats buildReviewStats(LocalDateTime start, LocalDateTime end) {
        Double averageRating = reviewRepository.averageRatingByStatus(ReviewStatus.VISIBLE);
        return AdminDashboardResponse.ReviewStats.builder()
                .total(reviewRepository.count())
                .newInRange(reviewRepository.countByCreatedAtBetween(start, end))
                .byStatus(buildCountByStatus(ReviewStatus.values(), reviewRepository::countByStatus))
                .averageRating(averageRating != null ? averageRating : 0.0)
                .build();
    }

    private AdminDashboardResponse.ContentStats buildContentStats(LocalDateTime start, LocalDateTime end) {
        return AdminDashboardResponse.ContentStats.builder()
                .blogPostsTotal(blogPostRepository.count())
                .blogPostsByStatus(buildCountByStatus(PublicationStatus.values(), blogPostRepository::countByStatus))
                .videosTotal(videoRepository.count())
                .videosByStatus(buildCountByStatus(PublicationStatus.values(), videoRepository::countByStatus))
                .cultureItemsTotal(cultureItemRepository.count())
                .cultureItemsByStatus(buildCountByStatus(PublicationStatus.values(), cultureItemRepository::countByStatus))
                .userMemoriesTotal(userMemoryRepository.count())
                .userMemoriesByStatus(buildCountByStatus(PublicationStatus.values(), userMemoryRepository::countByStatus))
                .build();
    }

    private AdminDashboardResponse.NotificationStats buildNotificationStats(LocalDateTime start, LocalDateTime end) {
        return AdminDashboardResponse.NotificationStats.builder()
                .total(notificationRepository.count())
                .newInRange(notificationRepository.countByCreatedAtBetween(start, end))
                .unread(notificationRepository.countByIsReadFalse())
                .build();
    }

    private <E extends Enum<E>> Map<String, Long> buildCountByStatus(E[] values, Function<E, Long> counter) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (E value : values) {
            result.put(value.name(), counter.apply(value));
        }
        return result;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
