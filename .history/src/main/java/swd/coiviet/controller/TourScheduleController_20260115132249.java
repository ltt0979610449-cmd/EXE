package swd.coiviet.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.request.CreateTourScheduleRequest;
import swd.coiviet.dto.request.UpdateTourScheduleRequest;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Tour;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.service.TourService;
import swd.coiviet.service.TourScheduleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public ResponseEntity<ApiResponse<TourSchedule>> createSchedule(
            @Validated @RequestBody CreateTourScheduleRequest request) {
        Tour tour = tourService.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));

        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .tourDate(request.getTourDate())
                .startTime(request.getStartTime())
                .maxSlots(request.getMaxSlots())
                .bookedSlots(0)
                .currentPrice(request.getCurrentPrice())
                .discountPercent(request.getDiscountPercent())
                .status(request.getStatus() != null ? request.getStatus() : TourScheduleStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();

        TourSchedule saved = tourScheduleService.save(schedule);
        return ResponseEntity.ok(ApiResponse.success(saved, "Tạo lịch trình tour thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TourSchedule>> updateSchedule(
            @PathVariable Long id,
            @RequestBody UpdateTourScheduleRequest request) {
        TourSchedule existing = tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));

        if (request.getTourId() != null) {
            Tour tour = tourService.findById(request.getTourId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
            existing.setTour(tour);
        }
        if (request.getTourDate() != null) existing.setTourDate(request.getTourDate());
        if (request.getStartTime() != null) existing.setStartTime(request.getStartTime());
        if (request.getMaxSlots() != null) {
            Integer bookedSlots = existing.getBookedSlots() != null ? existing.getBookedSlots() : 0;
            if (request.getMaxSlots() < bookedSlots) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Max slots không thể nhỏ hơn số chỗ đã đặt");
            }
            existing.setMaxSlots(request.getMaxSlots());
        }
        if (request.getCurrentPrice() != null) existing.setCurrentPrice(request.getCurrentPrice());
        if (request.getDiscountPercent() != null) existing.setDiscountPercent(request.getDiscountPercent());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());

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
