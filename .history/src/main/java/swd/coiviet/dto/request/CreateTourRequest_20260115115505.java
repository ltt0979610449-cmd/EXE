package swd.coiviet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTourRequest {
    @NotNull(message = "Province ID không được để trống")
    private Long provinceId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String slug;

    private String description;

    private BigDecimal durationHours;

    private Integer maxParticipants;

    private BigDecimal price;

    private Long artisanId;
}
