package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "artisans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artisan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String specialization;

    @Column(columnDefinition = "text")
    private String bio;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(columnDefinition = "text")
    private String workshopAddress;

    @Column(columnDefinition = "text")
    private String profileImageUrl;

    private Integer totalTours;

    private java.math.BigDecimal averageRating;

    private Boolean isActive;

    private LocalDateTime createdAt;
}
