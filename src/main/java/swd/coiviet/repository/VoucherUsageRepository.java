package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.model.VoucherUsage;

import java.util.List;
import java.util.Optional;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);
    Optional<VoucherUsage> findByUserIdAndVoucherId(Long userId, Long voucherId);

    @Query("SELECT v.voucher.id FROM VoucherUsage v WHERE v.user.id = :userId")
    List<Long> findVoucherIdsByUserId(@Param("userId") Long userId);
}
