package swd.coiviet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private String phone;

    @Size(min = 6)
    private String password;

    private String fullName;

    @Email
    private String email;

    private LocalDate dateOfBirth;
}
