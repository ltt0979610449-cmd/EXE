package swd.coiviet.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateVoucherRequest {
    private String code;
    private String discountType; // PERCENTAGE or FIXED
    private BigDecimal discountValue;
    private BigDecimal minPurchase;
    private Integer maxUsage;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
}