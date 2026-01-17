package swd.coiviet.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.service.TourScheduleService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tour-schedules")
public class TourScheduleController {

    private final TourScheduleService tourScheduleService;

    public TourScheduleController(TourScheduleService tourScheduleService) {
        this.tourScheduleService = tourScheduleService;
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<ApiResponse<List<TourSchedule>>> getSchedulesByTour(
            @PathVariable Long tourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TourSchedule> schedules;
        if (date != null) {
            schedules = tourScheduleService.findByTourIdAndDate(tourId, date);
        } else {
            // Get all schedules for tour - need to add this method
            schedules = List.of(); // Placeholder
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
    public ResponseEntity<ApiResponse<TourSchedule>> createSchedule(@RequestBody TourSchedule schedule) {
        TourSchedule saved = tourScheduleService.save(schedule);
        return ResponseEntity.ok(ApiResponse.success(saved, "Tạo lịch trình tour thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TourSchedule>> updateSchedule(@PathVariable Long id, @RequestBody TourSchedule schedule) {
        TourSchedule existing = tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));
        
        schedule.setId(existing.getId());
        TourSchedule updated = tourScheduleService.save(schedule);
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
