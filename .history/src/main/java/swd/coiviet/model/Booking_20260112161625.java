package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String bookingCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "tour_schedule_id")
    private TourSchedule tourSchedule;

    private Integer numParticipants;

    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal finalAmount;

    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;
    private java.math.BigDecimal cancellationFee;
    private java.math.BigDecimal refundAmount;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
