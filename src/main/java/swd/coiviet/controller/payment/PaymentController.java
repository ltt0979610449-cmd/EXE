package swd.coiviet.controller.payment;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.configuration.VnPayConfiguration;
import swd.coiviet.dto.request.CreatePaymentRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.PaymentResponse;
import swd.coiviet.enums.PaymentMethod;
import swd.coiviet.enums.PaymentStatus;
import swd.coiviet.enums.Role;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Booking;
import swd.coiviet.model.Payment;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.BookingService;
import swd.coiviet.service.NotificationService;
import swd.coiviet.service.PaymentGatewayService;
import swd.coiviet.service.PaymentService;

import java.net.URI;
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
    private final ArtisanService artisanService;
    private final JwtUtil jwtUtil;

    @Value("${app.payment.success-redirect-url:https://exe-project-two.vercel.app}")
    private String paymentSuccessRedirectUrl;

    public PaymentController(PaymentService paymentService, PaymentGatewayService paymentGatewayService,
                            BookingService bookingService, NotificationService notificationService,
                            ArtisanService artisanService, JwtUtil jwtUtil) {
        this.paymentService = paymentService;
        this.paymentGatewayService = paymentGatewayService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.artisanService = artisanService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    @Transactional
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
        
        // Get or create payment (for CASH, also accept PENDING_CASH for idempotency)
        Payment payment = paymentService.findByBookingId(request.getBookingId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.UNPAID
                        || (request.getPaymentMethod() == PaymentMethod.CASH && p.getStatus() == PaymentStatus.PENDING_CASH))
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
            // Cash payment - Pay-at-Property: chờ Artisan/Staff xác nhận đã nhận tiền
            if (payment.getStatus() != PaymentStatus.PENDING_CASH) {
                payment.setPaymentMethod(PaymentMethod.CASH);
                payment.setStatus(PaymentStatus.PENDING_CASH);
                payment = paymentService.save(payment);
                
                // Cập nhật booking qua reference từ payment để đảm bảo đồng bộ
                Booking bookingToUpdate = payment.getBooking();
                bookingToUpdate.setPaymentMethod(PaymentMethod.CASH);
                bookingToUpdate.setPaymentStatus(PaymentStatus.PENDING_CASH);
                bookingToUpdate.setStatus(swd.coiviet.enums.BookingStatus.PENDING);
                bookingToUpdate.setUpdatedAt(LocalDateTime.now());
                bookingService.save(bookingToUpdate);
                
                notificationService.createCashPendingNotification(userId, bookingToUpdate.getId());
            }
        }
        
        PaymentResponse response = mapToResponse(payment);
        response.setPaymentUrl(paymentUrl);
        
        String message = request.getPaymentMethod() == PaymentMethod.CASH
                ? "Đặt tour thành công. Vui lòng thanh toán tiền mặt cho hướng dẫn viên khi tham gia tour."
                : "Tạo payment thành công";
        return ResponseEntity.ok(ApiResponse.success(response, message));
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

    @PostMapping("/confirm-cash/{bookingId}")
    @PreAuthorize("hasAnyRole('ARTISAN', 'STAFF', 'ADMIN')")
    @Operation(summary = "Xác nhận đã nhận tiền mặt", description = "Artisan/Staff xác nhận đã nhận tiền mặt từ khách (Pay-at-Property)")
    @Transactional
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmCashPayment(
            @PathVariable Long bookingId,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);

        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking không tồn tại"));

        if (booking.getPaymentMethod() != PaymentMethod.CASH) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Booking này không phải thanh toán tiền mặt");
        }
        // Chấp nhận cả UNPAID (chỉ createBooking) và PENDING_CASH (đã gọi createPayment)
        if (booking.getPaymentStatus() != PaymentStatus.PENDING_CASH && booking.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Chỉ có thể xác nhận booking đang chờ thanh toán tiền mặt");
        }

        // Artisan: chỉ xác nhận được booking của tour mình quản lý
        if (Role.ARTISAN.name().equals(role)) {
            var artisan = artisanService.findByUserId(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN, "Bạn không phải nghệ nhân"));
            if (booking.getTour() == null || booking.getTour().getArtisan() == null
                    || !booking.getTour().getArtisan().getId().equals(artisan.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xác nhận thanh toán booking này");
            }
        }

        Payment payment = paymentService.findByBookingId(bookingId).stream()
                .filter(p -> p.getPaymentMethod() == PaymentMethod.CASH
                        && (p.getStatus() == PaymentStatus.PENDING_CASH || p.getStatus() == PaymentStatus.UNPAID))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment không tồn tại"));

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentService.save(payment);

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaidAt(LocalDateTime.now());
        booking.setStatus(swd.coiviet.enums.BookingStatus.CONFIRMED);
        bookingService.save(booking);
        bookingService.incrementTourTotalBookings(booking);

        notificationService.createPaymentSuccessNotification(
                booking.getUser().getId(), booking.getId(), payment.getAmount().toString());

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(payment),
                "Xác nhận thanh toán thành công. Tour đã được xác nhận."));
    }

    @PostMapping("/momo/notify")
    @Operation(summary = "MoMo payment callback", description = "Webhook callback từ MoMo sau khi thanh toán")
    @Transactional
    public ResponseEntity<String> momoNotify(@RequestBody Map<String, Object> requestBody) {
        try {
            String partnerRefId = (String) requestBody.get("partnerRefId");
            Long amount = Long.valueOf(requestBody.get("amount").toString());
            String status = requestBody.get("status").toString();
            
            if (paymentGatewayService.verifyMoMoCallback(partnerRefId, amount, status)) {
                Payment payment = paymentService.findByTransactionId(partnerRefId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment không tồn tại"));
                
                // Idempotency: đã thanh toán rồi thì không xử lý lại
                if (payment.getStatus() == PaymentStatus.PAID) {
                    return ResponseEntity.ok("OK");
                }
                
                if ("0".equals(status) || "success".equalsIgnoreCase(status)) {
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setGatewayTransactionId((String) requestBody.get("transId"));
                    payment.setGatewayResponse(requestBody.toString());
                    payment = paymentService.save(payment);
                    
                    Booking booking = payment.getBooking();
                    booking.setPaymentStatus(PaymentStatus.PAID);
                    booking.setPaidAt(LocalDateTime.now());
                    booking.setStatus(swd.coiviet.enums.BookingStatus.CONFIRMED);
                    bookingService.save(booking);
                    bookingService.incrementTourTotalBookings(booking);
                    
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
    @Transactional
    public ResponseEntity<?> momoReturn(@RequestParam Map<String, String> params) {
        // MoMo return URL dùng orderId (không phải partnerRefId) và resultCode (không phải status)
        String orderId = params.get("orderId");
        String resultCode = params.get("resultCode");
        
        if (orderId == null) {
            return ResponseEntity.ok("Thanh toán thất bại hoặc đã bị hủy.");
        }
        
        Payment payment = paymentService.findByTransactionId(orderId).orElse(null);
        if (payment == null) {
            return ResponseEntity.ok("Thanh toán thất bại hoặc đã bị hủy.");
        }
        
        // resultCode=0 nghĩa là thanh toán thành công
        if ("0".equals(resultCode)) {
            // Idempotency: đã thanh toán rồi thì không xử lý lại
            if (payment.getStatus() != PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                payment.setGatewayTransactionId(params.get("transId"));
                payment.setGatewayResponse(params.toString());
                payment = paymentService.save(payment);
                
                Booking booking = payment.getBooking();
                booking.setPaymentStatus(PaymentStatus.PAID);
                booking.setPaidAt(LocalDateTime.now());
                booking.setStatus(swd.coiviet.enums.BookingStatus.CONFIRMED);
                bookingService.save(booking);
                bookingService.incrementTourTotalBookings(booking);
                
                notificationService.createPaymentSuccessNotification(
                        booking.getUser().getId(), booking.getId(), payment.getAmount().toString());
            }
            return redirectToETicket(payment.getBooking());
        }
        
        return ResponseEntity.ok("Thanh toán thất bại hoặc đã bị hủy.");
    }

    @GetMapping("/vnpay/return")
    @Operation(summary = "VNPay payment return", description = "Redirect URL sau khi thanh toán VNPay")
    @Transactional
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            if (paymentGatewayService.verifyVnPayCallback(params)) {
                String vnp_TxnRef = params.get("vnp_TxnRef");
                String vnp_ResponseCode = params.get("vnp_ResponseCode");
                
                Payment payment = paymentService.findByTransactionId(vnp_TxnRef)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment không tồn tại"));
                
                // Idempotency: đã thanh toán rồi thì không xử lý lại
                if (payment.getStatus() == PaymentStatus.PAID) {
                    return redirectToETicket(payment.getBooking());
                }
                
                if ("00".equals(vnp_ResponseCode)) {
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setGatewayTransactionId(params.get("vnp_TransactionNo"));
                    payment.setGatewayResponse(params.toString());
                    payment = paymentService.save(payment);
                    
                    Booking booking = payment.getBooking();
                    booking.setPaymentStatus(PaymentStatus.PAID);
                    booking.setPaidAt(LocalDateTime.now());
                    booking.setStatus(swd.coiviet.enums.BookingStatus.CONFIRMED);
                    bookingService.save(booking);
                    bookingService.incrementTourTotalBookings(booking);
                    
                    // Send notification
                    notificationService.createPaymentSuccessNotification(
                            booking.getUser().getId(), booking.getId(), payment.getAmount().toString());
                    
                    return redirectToETicket(booking);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setGatewayResponse(params.toString());
                    paymentService.save(payment);
                    return ResponseEntity.ok("Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode);
                }
            }
            
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Thanh toán thất bại hoặc đã bị hủy.");
        }
    }

    private ResponseEntity<Void> redirectToETicket(Booking booking) {
        String baseUrl = paymentSuccessRedirectUrl.endsWith("/") 
                ? paymentSuccessRedirectUrl.substring(0, paymentSuccessRedirectUrl.length() - 1) 
                : paymentSuccessRedirectUrl;
        Long tourId = (booking.getTour() != null) ? booking.getTour().getId() : 0L;
        String eTicketUrl = UriComponentsBuilder.fromUriString(baseUrl + "/tours/" + tourId + "/booking/e-ticket")
                .queryParam("bookingCode", booking.getBookingCode())
                .build()
                .toUriString();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(eTicketUrl)).build();
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

    private String getCurrentUserRole(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                return null;
            }
            String role = jwtUtil.getRoleFromToken(token);
            return role != null ? role : Role.CUSTOMER.name();
        } catch (Exception e) {
            return Role.CUSTOMER.name();
        }
    }
}
