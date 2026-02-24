package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provinces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String slug;

    private String region;

    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;

    @Column(columnDefinition = "text")
    private String thumbnailUrl;

    @Column(columnDefinition = "text")
    private String description;

    private Boolean isActive;

    /** Thời điểm đẹp nhất để tham quan (e.g. "Tháng 10 - Tháng 3 (mùa khô)") */
    @Column(columnDefinition = "text")
    private String bestSeason;

    /** Cách di chuyển đến vùng (e.g. "Xe máy, xe khách từ Pleiku") */
    @Column(columnDefinition = "text")
    private String transportation;

    /** Lưu ý ứng xử văn hoá - JSON array hoặc text (e.g. ["Trang phục lịch sự", "Tôn trọng phong tục địa phương"]) */
    @Column(columnDefinition = "text")
    private String culturalTips;
}
