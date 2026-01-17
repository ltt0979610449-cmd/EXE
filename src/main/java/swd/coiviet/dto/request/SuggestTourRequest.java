package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SuggestTourRequest {
    @NotNull(message = "Tỉnh thành không được để trống")
    private Long provinceId;

    private LocalDate preferredDate;

    private Integer numParticipants;
}
