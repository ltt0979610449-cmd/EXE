package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd.coiviet.enums.LearnDifficulty;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private Long moduleId;
    private String title;
    private Integer timeLimitMinutes;
    private LearnDifficulty difficulty;
    private String objective;
    private List<String> rules;
    private Integer totalQuestions;
    private List<QuizQuestionResponse> questions;
}
