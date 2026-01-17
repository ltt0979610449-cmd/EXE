package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    private LocalDate tourDate;

    private LocalTime startTime;

    private Integer maxSlots;

    private Integer bookedSlots;

    private java.math.BigDecimal currentPrice;

    private Integer discountPercent;

    private String status;

    private LocalDateTime createdAt;
}
