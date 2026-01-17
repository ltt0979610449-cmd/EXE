package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanDashboardResponse {
    private Long artisanId;
    private String artisanName;
    private LocalDate fromDate;
    private LocalDate toDate;

    private TourStats tours;
    private BookingStats bookings;
    private RevenueStats revenue;
    private RatingStats ratings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourStats {
        private long total;
        private long active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingStats {
        private long total;
        private long newInRange;
        private long completed;
        private long cancelled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueStats {
        private BigDecimal totalRevenue;
        private BigDecimal revenueInRange;
        private BigDecimal averageRevenuePerBooking;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingStats {
        private Double averageRating;
        private long totalReviews;
        private long fiveStar;
        private long fourStar;
        private long threeStar;
        private long twoStar;
        private long oneStar;
    }
}
