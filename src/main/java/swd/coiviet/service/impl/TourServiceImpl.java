package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.enums.Status;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.model.Tour;
import swd.coiviet.repository.TourRepository;
import swd.coiviet.repository.TourScheduleRepository;
import swd.coiviet.repository.VoucherRepository;
import swd.coiviet.service.ReviewService;
import swd.coiviet.service.TourService;
import swd.coiviet.enums.ReviewStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TourServiceImpl implements TourService {
    private final TourRepository repo;
    private final TourScheduleRepository tourScheduleRepository;
    private final VoucherRepository voucherRepository;
    private final ReviewService reviewService;

    public TourServiceImpl(TourRepository repo, TourScheduleRepository tourScheduleRepository,
                           VoucherRepository voucherRepository, ReviewService reviewService) {
        this.repo = repo;
        this.tourScheduleRepository = tourScheduleRepository;
        this.voucherRepository = voucherRepository;
        this.reviewService = reviewService;
    }

    @Override
    public Tour save(Tour t) { return repo.save(t); }

    @Override
    public Optional<Tour> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Tour> findByProvinceId(Long provinceId) { return repo.findByProvinceId(provinceId); }

    @Override
    public List<Tour> findByArtisanId(Long artisanId) { return repo.findByArtisanId(artisanId); }

    @Override
    public List<Tour> findAll() { return repo.findAll(); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }

    @Override
    public void updateTourRating(Long tourId) {
        Optional<Tour> tourOpt = findById(tourId);
        if (tourOpt.isEmpty()) return;

        Tour tour = tourOpt.get();
        List<swd.coiviet.model.Review> reviews = reviewService.findByTourIdAndStatus(tourId, ReviewStatus.VISIBLE);

        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .mapToInt(swd.coiviet.model.Review::getRating)
                    .average()
                    .orElse(0.0);

            tour.setAverageRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
            save(tour);
        }
    }

    @Override
    public List<Tour> findToursWithDiscount(LocalDate fromDate, Long provinceId, Integer limit) {
        LocalDate effectiveFromDate = fromDate != null ? fromDate : LocalDate.now();
        int effectiveLimit = limit != null && limit > 0 ? limit : 50;

        Set<Long> tourIds = new LinkedHashSet<>(
                tourScheduleRepository.findDistinctTourIdsWithDiscount(effectiveFromDate));

        List<Long> scheduleIdsWithVoucher = voucherRepository.findActiveTourScheduleIds(LocalDateTime.now());
        if (!scheduleIdsWithVoucher.isEmpty()) {
            List<Long> validScheduleIds = scheduleIdsWithVoucher.stream()
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            if (!validScheduleIds.isEmpty()) {
                tourScheduleRepository.findAllById(validScheduleIds).stream()
                        .filter(ts -> ts.getTourDate() != null && !ts.getTourDate().isBefore(effectiveFromDate))
                        .filter(ts -> ts.getStatus() == TourScheduleStatus.SCHEDULED)
                        .map(ts -> ts.getTour().getId())
                        .forEach(tourIds::add);
            }
        }

        if (tourIds.isEmpty()) {
            return List.of();
        }

        List<Tour> tours = repo.findAllById(new ArrayList<>(tourIds)).stream()
                .filter(t -> t.getStatus() == Status.ACTIVE)
                .filter(t -> provinceId == null || (t.getProvince() != null && provinceId.equals(t.getProvince().getId())))
                .limit(effectiveLimit)
                .collect(Collectors.toList());

        return tours;
    }
}
