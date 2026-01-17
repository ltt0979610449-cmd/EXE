package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.configuration.VnPayConfiguration;
import swd.coiviet.dto.request.CreatePaymentRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.PaymentResponse;
import swd.coiviet.enums.PaymentMethod;
import swd.coiviet.enums.PaymentStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Booking;
import swd.coiviet.model.Payment;
import swd.coiviet.service.BookingService;
import swd.coiviet.service.NotificationService;
import swd.coiviet.service.PaymentGatewayService;
import swd.coiviet.service.PaymentService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    public PaymentController(PaymentService paymentService, PaymentGatewayService paymentGatewayService,
                            BookingService bookingService, NotificationService notificationService,
                            JwtUtil jwtUtil) {
        this.paymentService = paymentService;
        this.paymentGatewayService = paymentGatewayService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo payment và lấy payment URL", description = "Tạo payment record và lấy URL thanh toán từ MoMo/VNPay")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Validated @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        
        // Get booking
        Booking booking = bookingService.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking không tồn tại"));
        
        // Check ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền thanh toán booking này");
        }
        
        // Check if booking is already paid
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Booking đã được thanh toán");
        }
        
        // Get or create payment
        Payment payment = paymentService.findByBookingId(request.getBookingId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.UNPAID)
                .findFirst()
                .orElseGet(() -> {
                    Payment newPayment = Payment.builder()
                            .booking(booking)
                            .transactionId("TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                            .paymentMethod(request.getPaymentMethod())
                            .amount(booking.getFinalAmount())
                            .status(PaymentStatus.UNPAID)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return paymentService.save(newPayment);
                });
        
        // Generate payment URL based on payment method
        String paymentUrl = null;
        if (request.getPaymentMethod() == PaymentMethod.MOMO) {
            String returnUrl = getBaseUrl(httpRequest) + "/api/payments/momo/return";
            String notifyUrl = getBaseUrl(httpRequest) + "/api/payments/momo/notify";
            paymentUrl = paymentGatewayService.createMoMoPaymentUrl(payment, returnUrl, notifyUrl);
        } else if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            String returnUrl = getBaseUrl(httpRequest) + "/api/payments/vnpay/return";
            String ipAddress = VnPayConfiguration.getIpAddress(httpRequest);
            paymentUrl = paymentGatewayService.createVnPayPaymentUrl(payment, returnUrl, ipAddress);
        } else if (request.getPaymentMethod() == PaymentMethod.CASH) {
            // Cash payment - mark as paid immediately
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment = paymentService.save(payment);
            
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setStatus(swd.coiviet.enums.BookingStatus.CONFIRMED);
            bookingService.save(booking);
            
            // Send notification
            notificationService.createPaymentSuccessNotification(
                    userId, booking.getId(), booking.getFinalAmount().toString());
        }
        
        PaymentResponse response = mapToResponse(payment);
        response.setPaymentUrl(paymentUrl);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo payment thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin payment", description = "Lấy thông tin chi tiết của một payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Payment payment = paymentService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment không tồn tại"));
        
        // Check ownership
        if (!payment.getBooking().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền truy cập payment này");
        }
        
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(payment)));
    }

    @PostMapping("/momo/notify")
    @Operation(summary = "MoMo payment callback", description = "Webhook callback từ MoMo sau khi thanh toán")
    public ResponseEntity<String> momoNotify(@RequestBody Map<String, Object> requestBody) {
        try {
            String partnerRefId = (String) requestBody.get("partnerRefId");
            Long amount = Long.valueOf(requestBody.get("amount").toString());
            String status = requestBody.get("status").toString();
            
            if (paymentGatewayService.verifyMoMoCallback(partnerRefId, amount, status)) {
                Payment payment = paymentService.findByTransactionId(partnerRefId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment không tồn tại"));
                
                if ("0".equals(status) || "success".equalsIgnoreCase(status)) {
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setGatewayTransactionId((String) requestBody.get("transId"));
                    payment.setGatewayResponse(requestBody.toString());
                    payment = paymentService.save(payment);
                    
                    Booking booking = payment.getBooking();
                    booking.setPaymentStatus(PaymentStatus.PAID);
                    booking.setStatus(swd.coiviet.enums.BookingStatus.CONFIRMED);
                    bookingService.save(booking);
                    
                    // Send notification
                    notificationService.createPaymentSuccessNotification(
                            booking.getUser().getId(), booking.getId(), payment.getAmount().toString());
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setGatewayResponse(requestBody.toString());
                    paymentService.save(payment);
                }
                
                return ResponseEntity.ok("OK");
            }
            
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/momo/return")
    @Operation(summary = "MoMo payment return", description = "Redirect URL sau khi thanh toán MoMo")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> params) {
        // Handle return from MoMo
        String partnerRefId = params.get("partnerRefId");
        String status = params.get("status");
        
        Payment payment = paymentService.findByTransactionId(partnerRefId)
                .orElse(null);
        
        if (payment != null && "0".equals(status)) {
            return ResponseEntity.ok("Thanh toán thành công! Mã booking: " + payment.getBooking().getBookingCode());
        }
        
        return ResponseEntity.ok("Thanh toán thất bại hoặc đã bị hủy.");
    }

    @GetMapping("/vnpay/return")
    @Operation(summary = "VNPay payment return", description = "Redirect URL sau khi thanh toán VNPay")
    @Transactional
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            if (paymentGatewayService.verifyVnPayCallback(params)) {
                String vnp_TxnRef = params.get("vnp_TxnRef");
                String vnp_ResponseCode = params.get("vnp_ResponseCode");
                
                Payment payment = paymentService.findByTransactionId(vnp_TxnRef)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment không tồn tại"));
                
                if ("00".equals(vnp_ResponseCode)) {
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setGatewayTransactionId(params.get("vnp_TransactionNo"));
                    payment.setGatewayResponse(params.toString());
                    payment = paymentService.save(payment);
                    
                    Booking booking = payment.getBooking();
                    booking.setPaymentStatus(PaymentStatus.PAID);
                    booking.setStatus(swd.coiviet.enums.BookingStatus.CONFIRMED);
                    bookingService.save(booking);
                    
                    // Send notification
                    notificationService.createPaymentSuccessNotification(
                            booking.getUser().getId(), booking.getId(), payment.getAmount().toString());
                    
                    return ResponseEntity.ok("Thanh toán thành công! Mã booking: " + booking.getBookingCode());
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setGatewayResponse(params.toString());
                    paymentService.save(payment);
                    return ResponseEntity.ok("Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode);
                }
            }
            
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .bookingCode(payment.getBooking().getBookingCode())
                .transactionId(payment.getTransactionId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        url.append(contextPath);
        return url.toString();
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ");
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn");
            }
            
            io.jsonwebtoken.Claims claims = jwtUtil.getClaims(token);
            Integer userId = claims.get("userId", Integer.class);
            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không chứa thông tin user");
            }
            return Long.valueOf(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
        }
    }
}
