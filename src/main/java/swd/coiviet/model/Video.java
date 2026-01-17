package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import swd.coiviet.enums.PublicationStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String videoUrl;

    @Column(columnDefinition = "text")
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @ManyToOne
    @JoinColumn(name = "culture_item_id")
    private CultureItem cultureItem;

    @Enumerated(EnumType.STRING)
    private PublicationStatus status;

    private LocalDateTime createdAt;
}
