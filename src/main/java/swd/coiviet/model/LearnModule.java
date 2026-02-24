package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import swd.coiviet.enums.LearnModuleStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learn_modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private LearnCategory category;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "text")
    private String thumbnailUrl;

    @Column(columnDefinition = "text")
    private String quickNotesJson;

    @Column(columnDefinition = "text")
    private String culturalEtiquetteTitle;

    @Column(columnDefinition = "text")
    private String culturalEtiquetteText;

    @Enumerated(EnumType.STRING)
    private LearnModuleStatus status;

    private Integer orderIndex;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LearnLesson> lessons = new ArrayList<>();

    @OneToOne(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    @ManyToMany
    @JoinTable(
            name = "learn_module_tours",
            joinColumns = @JoinColumn(name = "module_id"),
            inverseJoinColumns = @JoinColumn(name = "tour_id")
    )
    @Builder.Default
    private List<Tour> suggestedTours = new ArrayList<>();
}
