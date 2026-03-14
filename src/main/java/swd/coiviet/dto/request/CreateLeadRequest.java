package swd.coiviet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import swd.coiviet.enums.LeadSource;

@Data
public class CreateLeadRequest {
    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;
    private Long tourId;
    private String message;

    private LeadSource source;
}
