package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateArtisanRequest {
    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Chuyên môn không được để trống")
    private String specialization;

    private String bio;
    private Long provinceId;
    private String workshopAddress;
}
