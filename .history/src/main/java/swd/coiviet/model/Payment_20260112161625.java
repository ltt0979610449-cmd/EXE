package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(unique = true)
    private String transactionId;

    private String paymentMethod;

    private java.math.BigDecimal amount;

    @Column(columnDefinition = "text")
    private String gatewayResponse;

    private String gatewayTransactionId;

    private String status;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String refundReason;

    private LocalDateTime createdAt;
}
