package swd.coiviet.service;

import swd.coiviet.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review save(Review r);
    Optional<Review> findById(Long id);
    List<Review> findByTourId(Long tourId);
    void deleteById(Long id);
}
