package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import swd.coiviet.enums.LeadSource;
import swd.coiviet.enums.LeadStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    private LeadStatus status;

    @Column(columnDefinition = "text")
    private String adminNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
