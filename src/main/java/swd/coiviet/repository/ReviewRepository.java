package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.ReviewStatus;
import swd.coiviet.model.Review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTourId(Long tourId);
    List<Review> findByTourIdAndStatus(Long tourId, ReviewStatus status);
    Optional<Review> findByBookingId(Long bookingId);
    List<Review> findByUserId(Long userId);
    List<Review> findByStatus(ReviewStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(ReviewStatus status);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.status = :status")
    Double averageRatingByStatus(@Param("status") ReviewStatus status);
}
