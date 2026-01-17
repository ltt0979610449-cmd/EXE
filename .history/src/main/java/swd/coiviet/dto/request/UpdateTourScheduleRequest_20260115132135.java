package swd.coiviet.dto.request;

import lombok.Data;
import swd.coiviet.enums.TourScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateTourScheduleRequest {
    private Long tourId;
    private LocalDate tourDate;
    private LocalTime startTime;
    private Integer maxSlots;
    private BigDecimal currentPrice;
    private Integer discountPercent;
    private TourScheduleStatus status;
}
