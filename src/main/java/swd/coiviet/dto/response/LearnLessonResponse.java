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
public class LearnLessonResponse {
    private Long id;
    private String title;
    private String slug;
    private String imageUrl;
    private String contentJson;
    private String vocabularyJson;
    private String objectiveText;
    private LearnDifficulty difficulty;
    private Integer estimatedMinutes;
    private String videoUrl;
    private Integer viewsCount;
    private Integer orderIndex;
    private Long totalLessonsInModule;
    private ArtisanSummaryResponse author;
    private Long moduleId;
    private String moduleTitle;
    private String categoryName;
}
