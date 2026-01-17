package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "culture_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CultureItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

        @Enumerated(EnumType.STRING)
        private CultureCategory category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String thumbnailUrl;

    @Column(columnDefinition = "text")
    private String images;

    private String videoUrl;

        @Enumerated(EnumType.STRING)
        private PublicationStatus status;

    private LocalDateTime createdAt;
}
