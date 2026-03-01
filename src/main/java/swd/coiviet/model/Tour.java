package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import swd.coiviet.enums.Status;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    /** Thời điểm đẹp nhất để tham quan (e.g. "Tháng 10 - Tháng 3 (mùa khô)") */
    @Column(columnDefinition = "text")
    private String bestSeason;

    /** Cách di chuyển đến vùng (e.g. "Xe máy, xe khách từ Pleiku") */
    @Column(columnDefinition = "text")
    private String transportation;

    /** Lưu ý ứng xử văn hoá - JSON array hoặc text */
    @Column(columnDefinition = "text")
    private String culturalTips;

    private java.math.BigDecimal durationHours;

    private Integer maxParticipants;

    private java.math.BigDecimal price;

    @Column(columnDefinition = "text")
    private String thumbnailUrl;

    @Column(columnDefinition = "text")
    private String images;

    @ManyToOne
    @JoinColumn(name = "artisan_id")
    private Artisan artisan;

    private Integer totalBookings;

    private java.math.BigDecimal averageRating;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
}
