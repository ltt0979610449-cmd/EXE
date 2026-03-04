package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.Voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    Optional<Voucher> findFirstByTourSchedule_IdAndDiscountValueAndIsActiveTrueAndValidUntilAfter(
            Long tourScheduleId, BigDecimal discountValue, LocalDateTime validAfter);
}
