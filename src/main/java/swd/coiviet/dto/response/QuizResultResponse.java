package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {
    private Long attemptId;
    private Integer correctCount;
    private Integer totalQuestions;
    private BigDecimal scorePercent;
    private Integer timeTakenSeconds;
    private List<QuizQuestionResultResponse> questionResults;
    private List<TourSummaryResponse> suggestedTours;
    private Boolean canClaimVoucher;
}
