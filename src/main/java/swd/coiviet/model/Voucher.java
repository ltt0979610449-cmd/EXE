package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String discountType;

    private java.math.BigDecimal discountValue;

    private java.math.BigDecimal minPurchase;

    private Integer maxUsage;

    private Integer currentUsage;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private Boolean isActive;

    private LocalDateTime createdAt;
}
