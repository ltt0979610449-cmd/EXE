package swd.coiviet.dto.response;

import lombok.Builder;
import lombok.Data;
import swd.coiviet.model.EmailLog;

import java.time.LocalDateTime;

@Data
@Builder
public class EmailLogResponse {
    private Long id;
    private String recipientEmail;
    private String subject;
    private String templateType;
    private Long relatedId;
    private String relatedType;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime openedAt;
    private Integer openedCount;
    private LocalDateTime createdAt;
    private Boolean opened;

    public static EmailLogResponse fromEntity(EmailLog e) {
        return EmailLogResponse.builder()
                .id(e.getId())
                .recipientEmail(e.getRecipientEmail())
                .subject(e.getSubject())
                .templateType(e.getTemplateType())
                .relatedId(e.getRelatedId())
                .relatedType(e.getRelatedType())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .sentAt(e.getSentAt())
                .openedAt(e.getOpenedAt())
                .openedCount(e.getOpenedCount())
                .createdAt(e.getCreatedAt())
                .opened(e.getOpenedAt() != null)
                .build();
    }
}
