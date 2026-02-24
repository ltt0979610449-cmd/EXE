package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSummaryResponse {
    private Long id;
    private String title;
    private Integer totalQuestions;
    private Integer timeLimitMinutes;
}
