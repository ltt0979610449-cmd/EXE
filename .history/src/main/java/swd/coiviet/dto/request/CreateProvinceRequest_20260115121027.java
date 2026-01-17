package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProvinceRequest {
    @NotBlank(message = "Tên tỉnh thành không được để trống")
    private String name;

    private String slug;
    private String region;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Boolean isActive;
}
