package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateVoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // PERCENTAGE or FIXED

    @NotNull(message = "Giá trị giảm giá không được để trống")
    private BigDecimal discountValue;

    private BigDecimal minPurchase;
    private Integer maxUsage;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime validUntil;
    
    private Boolean isActive;
}
