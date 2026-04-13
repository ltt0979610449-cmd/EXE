package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.model.Voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    Optional<Voucher> findFirstByTourSchedule_IdAndDiscountValueAndIsActiveTrueAndValidUntilAfter(
            Long tourScheduleId, BigDecimal discountValue, LocalDateTime validAfter);

    Optional<Voucher> findFirstByTourSchedule_IdAndIsActiveTrueAndValidUntilAfter(
            Long tourScheduleId, LocalDateTime validAfter);

    Optional<Voucher> findFirstByTourSchedule_IdAndCreatedAtAfter(
            Long tourScheduleId, LocalDateTime createdAtAfter);

    @Query("SELECT DISTINCT v.tourSchedule.id FROM Voucher v WHERE v.tourSchedule IS NOT NULL AND v.isActive = true AND v.validUntil > :now")
    List<Long> findActiveTourScheduleIds(@Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE v.tourSchedule IS NOT NULL AND v.tourSchedule.tour.id = :tourId " +
            "AND v.isActive = true AND v.validFrom <= :now AND v.validUntil > :now " +
            "AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage)")
    List<Voucher> findActiveVouchersByTourId(@Param("tourId") Long tourId, @Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.validFrom <= :now AND v.validUntil > :now " +
            "AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage)")
    List<Voucher> findAllActiveVouchers(@Param("now") LocalDateTime now);
}
