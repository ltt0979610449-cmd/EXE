package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnModuleResponse {
    private Long id;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private Long categoryId;
    private String categoryName;
    private String quickNotesJson;
    private String culturalEtiquetteTitle;
    private String culturalEtiquetteText;
    private Integer lessonsCount;
    private Integer durationMinutes;
    private List<LearnLessonSummaryResponse> lessons;
    private QuizSummaryResponse quizPrompt;
    private List<TourSummaryResponse> suggestedTours;
}
