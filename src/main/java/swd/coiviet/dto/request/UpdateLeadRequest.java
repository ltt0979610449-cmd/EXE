package swd.coiviet.dto.request;

import lombok.Data;
import swd.coiviet.enums.LeadStatus;

@Data
public class UpdateLeadRequest {
    private LeadStatus status;
    private String adminNote;
}
