package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientEmail;
    private String subject;

    @Column(name = "template_type")
    private String templateType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "related_type")
    private String relatedType;

    @Enumerated(EnumType.STRING)
    private EmailLogStatus status;

    private LocalDateTime sentAt;
    private LocalDateTime openedAt;
    private Integer openedCount;

    private LocalDateTime createdAt;

    public enum EmailLogStatus {
        PENDING, SENT, FAILED
    }
}
