package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.enums.PaymentMethod;
import swd.coiviet.enums.PaymentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long userId;
    private Long tourId;
    private String tourTitle;
    private Long tourScheduleId;
    private LocalDateTime tourDate;
    private LocalDateTime tourStartTime;
    private Integer numParticipants;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal finalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private java.math.BigDecimal cancellationFee;
    private java.math.BigDecimal refundAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
