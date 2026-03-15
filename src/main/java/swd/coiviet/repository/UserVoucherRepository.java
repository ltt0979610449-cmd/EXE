package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.UserVoucher;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, Long voucherId);
    List<UserVoucher> findByUserIdOrderByClaimedAtDesc(Long userId);
}
