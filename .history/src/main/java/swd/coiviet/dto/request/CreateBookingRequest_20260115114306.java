package swd.coiviet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import swd.coiviet.enums.PaymentMethod;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Tour ID không được để trống")
    private Long tourId;

    @NotNull(message = "Tour Schedule ID không được để trống")
    private Long tourScheduleId;

    @NotNull(message = "Số lượng người tham gia không được để trống")
    @Min(value = 1, message = "Số lượng người tham gia phải lớn hơn 0")
    private Integer numParticipants;

    @NotBlank(message = "Tên liên hệ không được để trống")
    private String contactName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String contactPhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String contactEmail;

    private String voucherCode;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
}
