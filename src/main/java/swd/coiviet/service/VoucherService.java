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
}
