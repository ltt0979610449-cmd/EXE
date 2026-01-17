package swd.coiviet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import swd.coiviet.enums.TourScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateTourScheduleRequest {
    @NotNull(message = "Tour ID không được để trống")
    private Long tourId;

    @NotNull(message = "Ngày tour không được để trống")
    private LocalDate tourDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Số chỗ tối đa không được để trống")
    @Min(value = 1, message = "Số chỗ tối đa phải lớn hơn 0")
    private Integer maxSlots;

    private BigDecimal currentPrice;
    private Integer discountPercent;
    private TourScheduleStatus status;
}
