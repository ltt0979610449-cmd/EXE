package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_usages", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "voucher_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    private LocalDateTime usedAt;
}
