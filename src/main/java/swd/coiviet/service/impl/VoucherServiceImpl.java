package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.UserVoucher;
import swd.coiviet.model.Voucher;
import swd.coiviet.repository.UserVoucherRepository;
import swd.coiviet.repository.VoucherRepository;
import swd.coiviet.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository repo;
    private final UserVoucherRepository userVoucherRepo;

    public VoucherServiceImpl(VoucherRepository repo, UserVoucherRepository userVoucherRepo) {
        this.repo = repo;
        this.userVoucherRepo = userVoucherRepo;
    }

    @Override
    public Voucher save(Voucher v) { return repo.save(v); }

    @Override
    public Optional<Voucher> findById(Long id) { return repo.findById(id); }

    @Override
    public Optional<Voucher> findByCode(String code) { return repo.findByCode(code); }

    @Override
    public List<Voucher> findAll() { return repo.findAll(); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }

    @Override
    public Optional<Voucher> findExistingVoucherForSchedule(Long tourScheduleId, Integer discountPercent) {
        return repo.findFirstByTourSchedule_IdAndDiscountValueAndIsActiveTrueAndValidUntilAfter(
                tourScheduleId, BigDecimal.valueOf(discountPercent), LocalDateTime.now());
    }

    @Override
    public Optional<Voucher> findAnyActiveVoucherForSchedule(Long tourScheduleId) {
        return repo.findFirstByTourSchedule_IdAndIsActiveTrueAndValidUntilAfter(
                tourScheduleId, LocalDateTime.now());
    }

    @Override
    public Optional<Voucher> findRecentVoucherForSchedule(Long tourScheduleId, int withinHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(withinHours);
        return repo.findFirstByTourSchedule_IdAndCreatedAtAfter(tourScheduleId, since);
    }

    @Override
    public List<UserVoucher> findClaimedVouchersByUserId(Long userId) {
        return userVoucherRepo.findByUserIdOrderByClaimedAtDesc(userId);
    }

    @Override
    public List<Voucher> findActiveVouchersByTourId(Long tourId) {
        return repo.findActiveVouchersByTourId(tourId, LocalDateTime.now());
    }
}
