package swd.coiviet.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.request.UpdateLeadRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.LeadResponse;
import swd.coiviet.enums.LeadStatus;
import swd.coiviet.model.Lead;
import swd.coiviet.service.LeadService;

@RestController
@RequestMapping("/api/admin/leads")
@Tag(name = "Admin", description = "Quản lý lead")
public class AdminLeadController {

    private final LeadService leadService;

    public AdminLeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    @Operation(summary = "Danh sách lead", description = "Filter theo status, tourId")
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> listLeads(
            @Parameter(description = "Trạng thái: NEW, CONTACTED, CONVERTED")
            @RequestParam(required = false) LeadStatus status,
            @Parameter(description = "Tour ID")
            @RequestParam(required = false) Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Lead> leads = leadService.findAll(status, tourId, page, size);
        Page<LeadResponse> response = leads.map(LeadResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách lead thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết lead")
    public ResponseEntity<ApiResponse<LeadResponse>> getLead(@PathVariable Long id) {
        Lead lead = leadService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead), "Lấy chi tiết thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật lead", description = "Cập nhật status, ghi chú")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(@PathVariable Long id, @Valid @RequestBody UpdateLeadRequest request) {
        Lead lead = leadService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead), "Cập nhật lead thành công"));
    }
}
