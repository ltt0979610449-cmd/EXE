package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import swd.coiviet.enums.LearnDifficulty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "module_id", unique = true)
    private LearnModule module;

    @Column(nullable = false)
    private String title;

    private Integer timeLimitMinutes;

    @Enumerated(EnumType.STRING)
    private LearnDifficulty difficulty;

    @Column(columnDefinition = "text")
    private String objective;

    @Column(columnDefinition = "text")
    private String rulesJson;

    @Enumerated(EnumType.STRING)
    private swd.coiviet.enums.PublicationStatus status;

    @ManyToOne
    @JoinColumn(name = "achievement_voucher_id")
    private Voucher achievementVoucher;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();
}
