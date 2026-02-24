package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResultResponse {
    private Long questionId;
    private String questionText;
    private String userAnswerText;
    private String correctAnswerText;
    private String explanationText;
    private Boolean isCorrect;
}
