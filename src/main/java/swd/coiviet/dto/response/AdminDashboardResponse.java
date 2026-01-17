package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private LocalDate fromDate;
    private LocalDate toDate;

    private UserStats users;
    private TourStats tours;
    private TourScheduleStats tourSchedules;
    private BookingStats bookings;
    private PaymentStats payments;
    private ReviewStats reviews;
    private ContentStats content;
    private NotificationStats notifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private long total;
        private long newInRange;
        private Map<String, Long> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourStats {
        private long total;
        private long newInRange;
        private Map<String, Long> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourScheduleStats {
        private long total;
        private long upcoming;
        private Map<String, Long> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingStats {
        private long total;
        private long newInRange;
        private Map<String, Long> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStats {
        private long total;
        private long newInRange;
        private Map<String, Long> byStatus;
        private BigDecimal totalRevenue;
        private BigDecimal revenueInRange;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewStats {
        private long total;
        private long newInRange;
        private Map<String, Long> byStatus;
        private Double averageRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentStats {
        private long blogPostsTotal;
        private Map<String, Long> blogPostsByStatus;
        private long videosTotal;
        private Map<String, Long> videosByStatus;
        private long cultureItemsTotal;
        private Map<String, Long> cultureItemsByStatus;
        private long userMemoriesTotal;
        private Map<String, Long> userMemoriesByStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationStats {
        private long total;
        private long newInRange;
        private long unread;
    }
}
