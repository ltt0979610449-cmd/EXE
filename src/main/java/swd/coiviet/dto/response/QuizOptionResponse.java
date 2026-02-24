package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionResponse {
    private Long id;
    private String label;
    private String optionText;
    private Boolean isCorrect;  // Only set when showing results
}
