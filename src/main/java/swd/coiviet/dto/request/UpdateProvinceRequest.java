package swd.coiviet.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProvinceRequest {
    private String name;
    private String slug;
    private String region;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Boolean isActive;
}