package swd.coiviet.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.EmailLogResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.EmailLog;
import swd.coiviet.repository.EmailLogRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/mails")
@Tag(name = "Admin", description = "Quản lý mail và tracking")
public class AdminMailController {

    private final EmailLogRepository emailLogRepository;

    public AdminMailController(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
    }

    @GetMapping
    @Operation(summary = "Danh sách email đã gửi", description = "Filter theo recipient, template, opened, date range")
    public ResponseEntity<ApiResponse<Page<EmailLogResponse>>> listEmails(
            @Parameter(description = "Email người nhận (tìm kiếm)")
            @RequestParam(required = false) String recipient,
            @Parameter(description = "Loại template: PRE_DEPARTURE_REMINDER, POST_TOUR_FEEDBACK, BOOKING_CONFIRMATION, ...")
            @RequestParam(required = false) String templateType,
            @Parameter(description = "true = đã mở, false = chưa mở")
            @RequestParam(required = false) Boolean opened,
            @Parameter(description = "Từ ngày (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Đến ngày (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Use sentinel values instead of null to avoid PostgreSQL "could not determine data type of parameter"
        LocalDateTime fromDate = from != null ? from.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime toDate = to != null ? to.atTime(LocalTime.MAX) : LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        // Normalize empty strings to null to avoid JPQL parameter binding issues
        String recipientParam = (recipient != null && !recipient.isBlank()) ? recipient.trim() : null;
        String templateParam = (templateType != null && !templateType.isBlank()) ? templateType.trim() : null;

        // 0=all, 1=opened only, 2=not opened - avoids null Boolean parameter type inference
        int openedFilter = (opened == null) ? 0 : (opened ? 1 : 2);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EmailLog> logs = emailLogRepository.findWithFilters(recipientParam, templateParam, openedFilter, fromDate, toDate, pageable);
        Page<EmailLogResponse> response = logs.map(EmailLogResponse::fromEntity);

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách mail thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết email log", description = "Xem trạng thái đã mở, số lần mở")
    public ResponseEntity<ApiResponse<EmailLogResponse>> getEmailLog(@PathVariable Long id) {
        EmailLog log = emailLogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Email log không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(EmailLogResponse.fromEntity(log), "Lấy chi tiết thành công"));
    }
}
