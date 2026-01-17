package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.request.CreateVoucherRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.VoucherResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Voucher;
import swd.coiviet.service.NotificationService;
import swd.coiviet.service.VoucherService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;
    private final NotificationService notificationService;
    public VoucherController(VoucherService voucherService, NotificationService notificationService) {
        this.voucherService = voucherService;
        this.notificationService = notificationService;
    }

    @GetMapping("/public/validate/{code}")
    @Operation(summary = "Kiểm tra voucher hợp lệ", description = "Kiểm tra voucher có hợp lệ và có thể sử dụng không")
    public ResponseEntity<ApiResponse<VoucherResponse>> validateVoucher(
            @PathVariable String code,
            @RequestParam(required = false) java.math.BigDecimal purchaseAmount) {
        Voucher voucher = voucherService.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
        
        LocalDateTime now = LocalDateTime.now();
        boolean isValid = voucher.getIsActive() != null && voucher.getIsActive()
                && voucher.getValidFrom() != null && now.isAfter(voucher.getValidFrom())
                && voucher.getValidUntil() != null && now.isBefore(voucher.getValidUntil())
                && (voucher.getMaxUsage() == null || voucher.getCurrentUsage() < voucher.getMaxUsage());
        
        if (purchaseAmount != null && voucher.getMinPurchase() != null) {
            isValid = isValid && purchaseAmount.compareTo(voucher.getMinPurchase()) >= 0;
        }
        
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Voucher không hợp lệ hoặc đã hết hạn");
        }
        
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(voucher)));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách vouchers", description = "Lấy tất cả vouchers (chỉ ADMIN/STAFF)")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAllVouchers() {
        // TODO: Add admin/staff check
        List<Voucher> vouchers = voucherService.findAll();
        List<VoucherResponse> responses = vouchers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy voucher theo ID", description = "Lấy thông tin chi tiết voucher")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(@PathVariable Long id) {
        Voucher voucher = voucherService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(voucher)));
    }

    @PostMapping
    @Operation(summary = "Tạo voucher mới", description = "Tạo voucher mới (chỉ ADMIN/STAFF)")
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Validated @RequestBody CreateVoucherRequest request) {
        // Check if code already exists
        voucherService.findByCode(request.getCode()).ifPresent(v -> {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mã voucher đã tồn tại");
        });
        
        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minPurchase(request.getMinPurchase())
                .maxUsage(request.getMaxUsage())
                .currentUsage(0)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .build();
        
        Voucher saved = voucherService.save(voucher);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(saved), "Tạo voucher thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật voucher", description = "Cập nhật thông tin voucher (chỉ ADMIN/STAFF)")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @PathVariable Long id,
            @RequestBody CreateVoucherRequest request) {
        Voucher existing = voucherService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
        
        // Check if code is being changed and already exists
        if (request.getCode() != null && !existing.getCode().equals(request.getCode())) {
            voucherService.findByCode(request.getCode()).ifPresent(v -> {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Mã voucher đã tồn tại");
            });
        }
        
        if (request.getCode() != null) existing.setCode(request.getCode());
        if (request.getDiscountType() != null) existing.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) existing.setDiscountValue(request.getDiscountValue());
        if (request.getMinPurchase() != null) existing.setMinPurchase(request.getMinPurchase());
        if (request.getMaxUsage() != null) existing.setMaxUsage(request.getMaxUsage());
        if (request.getValidFrom() != null) existing.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) existing.setValidUntil(request.getValidUntil());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        
        Voucher updated = voucherService.save(existing);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(updated), "Cập nhật voucher thành công"));
    }

    @PostMapping("/{id}/send-to-user/{userId}")
    @Operation(summary = "Gửi voucher cho user", description = "Gửi voucher cho user và tạo notification")
    public ResponseEntity<ApiResponse<VoucherResponse>> sendVoucherToUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Voucher voucher = voucherService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
        
        String discountInfo = "PERCENTAGE".equals(voucher.getDiscountType())
                ? voucher.getDiscountValue() + "%"
                : voucher.getDiscountValue() + " VND";
        
        notificationService.createVoucherNotification(userId, voucher.getCode(), discountInfo);
        
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(voucher), "Đã gửi voucher cho user"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa voucher", description = "Xóa voucher (chỉ ADMIN/STAFF)")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable Long id) {
        voucherService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
        voucherService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa voucher thành công"));
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minPurchase(voucher.getMinPurchase())
                .maxUsage(voucher.getMaxUsage())
                .currentUsage(voucher.getCurrentUsage())
                .validFrom(voucher.getValidFrom())
                .validUntil(voucher.getValidUntil())
                .isActive(voucher.getIsActive())
                .createdAt(voucher.getCreatedAt())
                .build();
    }
}
