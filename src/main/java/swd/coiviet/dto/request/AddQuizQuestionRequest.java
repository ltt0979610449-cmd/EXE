package swd.coiviet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddQuizQuestionRequest {
    private String questionText;
    private String hintText;
    private String explanationText;
    private Integer orderIndex;
    private List<QuizOptionInput> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizOptionInput {
        private String label;
        private String optionText;
        private Boolean isCorrect;
    }
}
