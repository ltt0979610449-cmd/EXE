package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import swd.coiviet.enums.Role;

@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role không được để trống")
    private Role role;
}
