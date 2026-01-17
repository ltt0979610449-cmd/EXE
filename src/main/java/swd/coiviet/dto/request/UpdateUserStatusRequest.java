package swd.coiviet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import swd.coiviet.enums.Status;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "Status không được để trống")
    private Status status;
}
