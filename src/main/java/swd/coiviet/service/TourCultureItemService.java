package swd.coiviet.service;

import swd.coiviet.enums.CultureCategory;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.TourCultureItem;

import java.util.List;

public interface TourCultureItemService {
    TourCultureItem save(TourCultureItem item);
    List<TourCultureItem> findByTourId(Long tourId);
    /** Lấy CultureItem đã gắn với tour, sắp xếp theo displayOrder. Nếu tour không có items → fallback lấy theo province. */
    List<CultureItem> findCultureItemsByTourId(Long tourId);
    /** Lấy CultureItem theo tour và category (optional). Fallback theo province nếu tour không có items. */
    List<CultureItem> findCultureItemsByTourIdAndCategory(Long tourId, CultureCategory category);
    void deleteByTourIdAndCultureItemId(Long tourId, Long cultureItemId);
    /** Gắn danh sách culture items vào tour (thay thế toàn bộ items cũ). Thứ tự = displayOrder. */
    void setCultureItemsForTour(Long tourId, List<Long> cultureItemIds);
}
