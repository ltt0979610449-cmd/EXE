package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd.coiviet.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long tourId;
    private String tourTitle;
    private Integer rating;
    private String comment;
    private List<String> images;
    private ReviewStatus status;
    private LocalDateTime createdAt;
}
