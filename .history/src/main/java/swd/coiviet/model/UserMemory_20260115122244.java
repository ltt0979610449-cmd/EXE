package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import swd.coiviet.enums.PublicationStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_memories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String images; // Hình ảnh làng xưa, món ăn, khung cảnh Tết quê

    @Column(columnDefinition = "text")
    private String audioUrl; // Giọng nói địa phương

    @Column(columnDefinition = "text")
    private String videoUrl; // Video ký ức

    @Enumerated(EnumType.STRING)
    private PublicationStatus status; // DRAFT, PUBLISHED, ARCHIVED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (status == null) status = PublicationStatus.DRAFT;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
