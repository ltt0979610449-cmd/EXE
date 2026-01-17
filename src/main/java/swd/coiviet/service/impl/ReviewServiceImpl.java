package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Review;
import swd.coiviet.repository.ReviewRepository;
import swd.coiviet.service.ReviewService;
import swd.coiviet.enums.ReviewStatus;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository repo;

    public ReviewServiceImpl(ReviewRepository repo) { this.repo = repo; }

    @Override
    public Review save(Review r) { return repo.save(r); }

    @Override
    public Optional<Review> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Review> findByTourId(Long tourId) { return repo.findByTourId(tourId); }

    @Override
    public List<Review> findByTourIdAndStatus(Long tourId, ReviewStatus status) {
        return repo.findByTourIdAndStatus(tourId, status);
    }

    @Override
    public Optional<Review> findByBookingId(Long bookingId) { return repo.findByBookingId(bookingId); }

    @Override
    public List<Review> findByUserId(Long userId) { return repo.findByUserId(userId); }

    @Override
    public List<Review> findByStatus(ReviewStatus status) { return repo.findByStatus(status); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
