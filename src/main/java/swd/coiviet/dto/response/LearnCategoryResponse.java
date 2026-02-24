package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnCategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Integer orderIndex;
}
