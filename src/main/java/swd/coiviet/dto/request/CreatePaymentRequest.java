package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import swd.coiviet.enums.PaymentMethod;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "Booking ID không được để trống")
    private Long bookingId;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
}
