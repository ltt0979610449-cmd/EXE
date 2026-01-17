package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Tour;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.service.TourService;
import swd.coiviet.service.TourScheduleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tour-schedules")
public class TourScheduleController {

    private final TourScheduleService tourScheduleService;
    private final TourService tourService;

    public TourScheduleController(TourScheduleService tourScheduleService, TourService tourService) {
        this.tourScheduleService = tourScheduleService;
        this.tourService = tourService;
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<ApiResponse<List<TourSchedule>>> getSchedulesByTour(
            @PathVariable Long tourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TourSchedule> schedules;
        if (date != null) {
            schedules = tourScheduleService.findByTourIdAndDate(tourId, date);
        } else {
            schedules = tourScheduleService.findByTourId(tourId);
        }
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TourSchedule>> getScheduleById(@PathVariable Long id) {
        TourSchedule schedule = tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @PostMapping
    @Operation(summary = "Tạo lịch trình tour mới", description = "Tạo lịch trình tour với thông tin ngày, giờ và giá")
    public ResponseEntity<ApiResponse<TourSchedule>> createSchedule(
            @Parameter(description = "ID tour", required = true)
            @RequestParam @NotNull(message = "Tour ID không được để trống") Long tourId,
            @Parameter(description = "Ngày tour (format: yyyy-MM-dd)", required = true)
            @RequestParam @NotNull(message = "Ngày tour không được để trống") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tourDate,
            @Parameter(description = "Giờ bắt đầu (format: HH:mm)", required = true)
            @RequestParam @NotNull(message = "Giờ bắt đầu không được để trống") 
            @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @Parameter(description = "Số chỗ tối đa", required = true)
            @RequestParam @NotNull(message = "Số chỗ tối đa không được để trống") 
            @Min(value = 1, message = "Số chỗ tối đa phải lớn hơn 0") Integer maxSlots,
            @Parameter(description = "Giá hiện tại", required = false)
            @RequestParam(required = false) BigDecimal currentPrice,
            @Parameter(description = "Phần trăm giảm giá", required = false)
            @RequestParam(required = false) Integer discountPercent,
            @Parameter(description = "Trạng thái lịch trình", required = false)
            @RequestParam(required = false) TourScheduleStatus status) {
        Tour tour = tourService.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));

        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .tourDate(tourDate)
                .startTime(startTime)
                .maxSlots(maxSlots)
                .bookedSlots(0)
                .currentPrice(currentPrice)
                .discountPercent(discountPercent)
                .status(status != null ? status : TourScheduleStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();

        TourSchedule saved = tourScheduleService.save(schedule);
        return ResponseEntity.ok(ApiResponse.success(saved, "Tạo lịch trình tour thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật lịch trình tour", description = "Cập nhật thông tin lịch trình tour")
    public ResponseEntity<ApiResponse<TourSchedule>> updateSchedule(
            @PathVariable Long id,
            @Parameter(description = "ID tour", required = false)
            @RequestParam(required = false) Long tourId,
            @Parameter(description = "Ngày tour (format: yyyy-MM-dd)", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tourDate,
            @Parameter(description = "Giờ bắt đầu (format: HH:mm)", required = false)
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @Parameter(description = "Số chỗ tối đa", required = false)
            @RequestParam(required = false) @Min(value = 1, message = "Số chỗ tối đa phải lớn hơn 0") Integer maxSlots,
            @Parameter(description = "Giá hiện tại", required = false)
            @RequestParam(required = false) BigDecimal currentPrice,
            @Parameter(description = "Phần trăm giảm giá", required = false)
            @RequestParam(required = false) Integer discountPercent,
            @Parameter(description = "Trạng thái lịch trình", required = false)
            @RequestParam(required = false) TourScheduleStatus status) {
        TourSchedule existing = tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));

        if (tourId != null) {
            Tour tour = tourService.findById(tourId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
            existing.setTour(tour);
        }
        if (tourDate != null) existing.setTourDate(tourDate);
        if (startTime != null) existing.setStartTime(startTime);
        if (maxSlots != null) {
            Integer bookedSlots = existing.getBookedSlots() != null ? existing.getBookedSlots() : 0;
            if (maxSlots < bookedSlots) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Max slots không thể nhỏ hơn số chỗ đã đặt");
            }
            existing.setMaxSlots(maxSlots);
        }
        if (currentPrice != null) existing.setCurrentPrice(currentPrice);
        if (discountPercent != null) existing.setDiscountPercent(discountPercent);
        if (status != null) existing.setStatus(status);

        TourSchedule updated = tourScheduleService.save(existing);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật lịch trình tour thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));
        tourScheduleService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa lịch trình tour thành công"));
    }
}
