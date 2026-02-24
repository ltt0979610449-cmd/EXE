package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import swd.coiviet.enums.LearnDifficulty;

import java.time.LocalDateTime;

@Entity
@Table(name = "learn_lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private LearnModule module;

    @ManyToOne
    @JoinColumn(name = "artisan_id")
    private Artisan artisan;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    private Integer orderIndex;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Column(columnDefinition = "text")
    private String contentJson;

    @Column(columnDefinition = "text")
    private String vocabularyJson;

    @Column(columnDefinition = "text")
    private String objectiveText;

    @Enumerated(EnumType.STRING)
    private LearnDifficulty difficulty;

    private Integer estimatedMinutes;

    @Column(columnDefinition = "text")
    private String videoUrl;

    private Integer viewsCount;

    @Enumerated(EnumType.STRING)
    private swd.coiviet.enums.PublicationStatus status;

    private LocalDateTime createdAt;
}
