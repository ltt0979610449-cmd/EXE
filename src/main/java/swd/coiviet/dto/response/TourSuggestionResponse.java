package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourSuggestionResponse {
    private Long tourId;
    private String tourTitle;
    private String tourDescription;
    private String thumbnailUrl;
    private Long provinceId;
    private String provinceName;
    private Long artisanId;
    private String artisanName;
    private String artisanSpecialization;
    private BigDecimal price;
    private BigDecimal durationHours;
    private Integer maxParticipants;
    private Integer availableSlots;
    private LocalDate nextAvailableDate;
    private LocalTime nextAvailableTime;
    private BigDecimal averageRating;
    private Integer totalBookings;
    private String reason; // Lý do gợi ý tour này
}
