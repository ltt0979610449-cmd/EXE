package swd.coiviet.service;

import swd.coiviet.model.LearnCategory;

import java.util.List;
import java.util.Optional;

public interface LearnCategoryService {
    LearnCategory save(LearnCategory c);
    Optional<LearnCategory> findById(Long id);
    Optional<LearnCategory> findBySlug(String slug);
    List<LearnCategory> findAllActive();
    List<LearnCategory> findAll();
    void deleteById(Long id);
}
