package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Tour;
import swd.coiviet.repository.TourRepository;
import swd.coiviet.service.ReviewService;
import swd.coiviet.service.TourService;
import swd.coiviet.enums.ReviewStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class TourServiceImpl implements TourService {
    private final TourRepository repo;
    private final ReviewService reviewService;

    public TourServiceImpl(TourRepository repo, ReviewService reviewService) {
        this.repo = repo;
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
            tour.setTotalBookings(reviews.size());
            save(tour);
        }
    }
}
