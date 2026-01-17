package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Review;
import swd.coiviet.repository.ReviewRepository;
import swd.coiviet.service.ReviewService;

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
    public void deleteById(Long id) { repo.deleteById(id); }
}
