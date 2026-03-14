package swd.coiviet.dto.response;

import lombok.Builder;
import lombok.Data;
import swd.coiviet.model.Lead;

import java.time.LocalDateTime;

@Data
@Builder
public class LeadResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Long tourId;
    private String tourTitle;
    private String message;
    private String source;
    private String status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadResponse fromEntity(Lead l) {
        return LeadResponse.builder()
                .id(l.getId())
                .name(l.getName())
                .email(l.getEmail())
                .phone(l.getPhone())
                .tourId(l.getTour() != null ? l.getTour().getId() : null)
                .tourTitle(l.getTour() != null ? l.getTour().getTitle() : null)
                .message(l.getMessage())
                .source(l.getSource() != null ? l.getSource().name() : null)
                .status(l.getStatus() != null ? l.getStatus().name() : null)
                .adminNote(l.getAdminNote())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
