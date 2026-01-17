package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd.coiviet.enums.Gender;
import swd.coiviet.enums.Role;
import swd.coiviet.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Role role;
    private Status status;
    private LocalDateTime createdAt;
}
