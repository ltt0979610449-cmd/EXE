package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(nullable = false, length = 10)
    private String label;

    @Column(columnDefinition = "text", nullable = false)
    private String optionText;

    private Boolean isCorrect;
}
