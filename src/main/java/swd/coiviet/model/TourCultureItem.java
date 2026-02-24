package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_culture_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tour_id", "culture_item_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourCultureItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culture_item_id", nullable = false)
    private CultureItem cultureItem;

    /** Thứ tự hiển thị (số nhỏ = ưu tiên cao) */
    private Integer displayOrder;

    /** Phân loại tùy chọn: HIGHLIGHT, FESTIVAL, FOOD, etc. — null thì dùng category của CultureItem */
    private String highlightType;
}
