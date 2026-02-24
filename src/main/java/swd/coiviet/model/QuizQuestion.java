package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(columnDefinition = "text", nullable = false)
    private String questionText;

    @Column(columnDefinition = "text")
    private String hintText;

    @Column(columnDefinition = "text")
    private String explanationText;

    private Integer orderIndex;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("label")
    @Builder.Default
    private List<QuizOption> options = new ArrayList<>();
}
