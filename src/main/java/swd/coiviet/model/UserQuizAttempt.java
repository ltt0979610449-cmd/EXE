package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private Integer correctCount;

    private Integer totalQuestions;

    private java.math.BigDecimal scorePercent;

    private Integer timeTakenSeconds;

    @Column(columnDefinition = "text")
    private String answersJson;

    private LocalDateTime submittedAt;

    private Boolean voucherClaimed;
}
