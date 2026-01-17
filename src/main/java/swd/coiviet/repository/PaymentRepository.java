package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.PaymentStatus;
import swd.coiviet.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTransactionId(String transactionId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(PaymentStatus status);
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.paidAt BETWEEN :start AND :end")
    BigDecimal sumAmountByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.booking.tour.artisan.id = :artisanId AND p.status = :status")
    BigDecimal sumAmountByArtisanIdAndStatus(@Param("artisanId") Long artisanId, @Param("status") PaymentStatus status);
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.booking.tour.artisan.id = :artisanId AND p.status = :status AND p.paidAt BETWEEN :start AND :end")
    BigDecimal sumAmountByArtisanIdAndStatusAndPaidAtBetween(@Param("artisanId") Long artisanId, @Param("status") PaymentStatus status,
                                                              @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
