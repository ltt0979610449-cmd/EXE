package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourSummaryResponse {
    private Long id;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private String location;
    private String description;
    private BigDecimal price;
}
