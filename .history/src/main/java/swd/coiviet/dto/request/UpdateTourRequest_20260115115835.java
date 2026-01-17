package swd.coiviet.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTourRequest {
    private Long provinceId;
    private String title;
    private String slug;
    private String description;
    private BigDecimal durationHours;
    private Integer maxParticipants;
    private BigDecimal price;
    private Long artisanId;
}
