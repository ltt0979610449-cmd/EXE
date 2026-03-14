package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.request.CreateLeadRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.LeadResponse;
import swd.coiviet.model.Lead;
import swd.coiviet.service.LeadService;

/**
 * API public - Khách để lại thông tin quan tâm tour (không cần đăng nhập).
 */
@RestController
@RequestMapping("/api/leads")
@Tag(name = "Lead", description = "Để lại thông tin quan tâm tour")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    @Operation(summary = "Để lại thông tin", description = "Khách để lại thông tin quan tâm tour - không cần đăng nhập")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody CreateLeadRequest request) {
        Lead lead = leadService.create(request);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead), "Cảm ơn bạn đã để lại thông tin. Chúng tôi sẽ liên hệ sớm!"));
    }
}
