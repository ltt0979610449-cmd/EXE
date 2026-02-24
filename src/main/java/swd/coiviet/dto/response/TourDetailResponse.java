package swd.coiviet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Tour;

import java.util.List;

/**
 * Response mở rộng cho Tour detail, bao gồm tour và culture items (địa điểm nổi bật, lễ hội, ẩm thực).
 * FE có thể dùng thay vì gọi 2 API riêng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDetailResponse {
    private Tour tour;
    private List<CultureItem> cultureItems;
}
