package swd.coiviet.service;

import swd.coiviet.model.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    Voucher save(Voucher v);
    Optional<Voucher> findById(Long id);
    Optional<Voucher> findByCode(String code);
    java.util.List<Voucher> findAll();
    void deleteById(Long id);

    Optional<Voucher> findExistingVoucherForSchedule(Long tourScheduleId, Integer discountPercent);

    Optional<Voucher> findAnyActiveVoucherForSchedule(Long tourScheduleId);

    Optional<Voucher> findRecentVoucherForSchedule(Long tourScheduleId, int withinHours);

    List<swd.coiviet.model.UserVoucher> findClaimedVouchersByUserId(Long userId);

    List<Voucher> findActiveVouchersByTourId(Long tourId);

    /**
     * Kiểm tra user đã dùng voucher này chưa (1 voucher chỉ dùng 1 lần/account).
     */
    boolean hasUserUsedVoucher(Long userId, Long voucherId);

    /**
     * Ghi nhận user đã sử dụng voucher (dùng khi apply voucher vào booking).
     */
    void recordVoucherUsage(Long userId, Voucher voucher);

    /**
     * Lấy tất cả voucher user có thể dùng: voucher đã claim (chưa dùng) + voucher hệ thống (chưa dùng).
     */
    swd.coiviet.dto.response.AccountVouchersResponse findAvailableVouchersForUser(Long userId);

    /**
     * Lấy voucher hệ thống còn hiệu lực mà user chưa dùng.
     */
    List<Voucher> findActiveSystemVouchersNotUsedByUser(Long userId);
}
