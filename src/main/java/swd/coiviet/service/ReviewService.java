package swd.coiviet.service;

import swd.coiviet.model.Review;
import swd.coiviet.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review save(Review r);
    Optional<Review> findById(Long id);
    List<Review> findByTourId(Long tourId);
    List<Review> findByTourIdAndStatus(Long tourId, ReviewStatus status);
    Optional<Review> findByBookingId(Long bookingId);
    List<Review> findByUserId(Long userId);
    List<Review> findByStatus(ReviewStatus status);
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);
    Page<Review> findAll(Pageable pageable);
    void deleteById(Long id);
}
