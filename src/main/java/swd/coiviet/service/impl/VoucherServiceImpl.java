package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.dto.response.AccountVouchersResponse;
import swd.coiviet.dto.response.UserVoucherClaimedResponse;
import swd.coiviet.dto.response.VoucherResponse;
import swd.coiviet.model.User;
import swd.coiviet.model.UserVoucher;
import swd.coiviet.model.Voucher;
import swd.coiviet.model.VoucherUsage;
import swd.coiviet.repository.UserVoucherRepository;
import swd.coiviet.repository.VoucherRepository;
import swd.coiviet.repository.VoucherUsageRepository;
import swd.coiviet.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository repo;
    private final UserVoucherRepository userVoucherRepo;
    private final VoucherUsageRepository usageRepo;

    public VoucherServiceImpl(VoucherRepository repo, UserVoucherRepository userVoucherRepo,
                              VoucherUsageRepository usageRepo) {
        this.repo = repo;
        this.userVoucherRepo = userVoucherRepo;
        this.usageRepo = usageRepo;
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

    @Override
    public boolean hasUserUsedVoucher(Long userId, Long voucherId) {
        Optional<UserVoucher> uv = userVoucherRepo.findByUserIdAndVoucherId(userId, voucherId);
        if (uv.isPresent()) {
            return uv.get().getUsedAt() != null;
        }
        return usageRepo.existsByUserIdAndVoucherId(userId, voucherId);
    }

    @Override
    public void recordVoucherUsage(Long userId, Voucher voucher) {
        Optional<UserVoucher> uv = userVoucherRepo.findByUserIdAndVoucherId(userId, voucher.getId());
        if (uv.isPresent()) {
            UserVoucher u = uv.get();
            u.setUsedAt(LocalDateTime.now());
            userVoucherRepo.save(u);
        } else {
            User user = new User();
            user.setId(userId);
            usageRepo.save(VoucherUsage.builder()
                    .user(user)
                    .voucher(voucher)
                    .usedAt(LocalDateTime.now())
                    .build());
        }
    }

    @Override
    public AccountVouchersResponse findAvailableVouchersForUser(Long userId) {
        List<UserVoucher> claimed = userVoucherRepo.findByUserIdOrderByClaimedAtDesc(userId);
        List<UserVoucherClaimedResponse> userVouchers = claimed.stream()
                .filter(uv -> uv.getUsedAt() == null)
                .filter(uv -> isVoucherStillValid(uv.getVoucher()))
                .map(this::toUserVoucherClaimedResponse)
                .collect(Collectors.toList());

        List<Voucher> systemVouchers = findActiveSystemVouchersNotUsedByUser(userId);
        List<VoucherResponse> systemResponses = systemVouchers.stream()
                .map(this::toVoucherResponse)
                .collect(Collectors.toList());

        return AccountVouchersResponse.builder()
                .userVouchers(userVouchers)
                .systemVouchers(systemResponses)
                .build();
    }

    @Override
    public List<Voucher> findActiveSystemVouchersNotUsedByUser(Long userId) {
        List<Voucher> allActive = repo.findAllActiveVouchers(LocalDateTime.now());
        Set<Long> usedVoucherIds = Set.copyOf(usageRepo.findVoucherIdsByUserId(userId));
        Set<Long> userClaimedVoucherIds = userVoucherRepo.findByUserIdOrderByClaimedAtDesc(userId).stream()
                .map(uv -> uv.getVoucher().getId())
                .collect(Collectors.toSet());

        return allActive.stream()
                .filter(v -> !usedVoucherIds.contains(v.getId()))
                .filter(v -> !userClaimedVoucherIds.contains(v.getId()))
                .collect(Collectors.toList());
    }

    private boolean isVoucherStillValid(Voucher v) {
        if (v == null || v.getIsActive() == null || !v.getIsActive()) return false;
        LocalDateTime now = LocalDateTime.now();
        if (v.getValidFrom() != null && now.isBefore(v.getValidFrom())) return false;
        if (v.getValidUntil() != null && now.isAfter(v.getValidUntil())) return false;
        if (v.getMaxUsage() != null && v.getCurrentUsage() != null && v.getCurrentUsage() >= v.getMaxUsage()) return false;
        return true;
    }

    private UserVoucherClaimedResponse toUserVoucherClaimedResponse(UserVoucher uv) {
        Voucher v = uv.getVoucher();
        return UserVoucherClaimedResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minPurchase(v.getMinPurchase())
                .maxUsage(v.getMaxUsage())
                .currentUsage(v.getCurrentUsage())
                .validFrom(v.getValidFrom())
                .validUntil(v.getValidUntil())
                .isActive(v.getIsActive())
                .claimedAt(uv.getClaimedAt())
                .usedAt(uv.getUsedAt())
                .build();
    }

    private VoucherResponse toVoucherResponse(Voucher v) {
        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minPurchase(v.getMinPurchase())
                .maxUsage(v.getMaxUsage())
                .currentUsage(v.getCurrentUsage())
                .validFrom(v.getValidFrom())
                .validUntil(v.getValidUntil())
                .isActive(v.getIsActive())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
