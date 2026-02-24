package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnLessonSummaryResponse {
    private Long id;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private Integer duration;
    private String videoUrl;
    private Integer orderIndex;
}
