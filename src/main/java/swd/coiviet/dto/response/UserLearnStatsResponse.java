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
public class UserLearnStatsResponse {
    private Integer totalLessonsCompleted;
    private BigDecimal averageScore;
    private Integer learningStreak;
    private Integer totalCoursesCompleted;
    private Integer overallLearningProgressPercent;
    private List<LearnModuleResponse> featuredCourses;
}
