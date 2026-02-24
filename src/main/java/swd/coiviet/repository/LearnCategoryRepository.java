package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.LearnCategory;

import java.util.List;
import java.util.Optional;

public interface LearnCategoryRepository extends JpaRepository<LearnCategory, Long> {
    List<LearnCategory> findByIsActiveTrueOrderByOrderIndexAsc();
    Optional<LearnCategory> findBySlug(String slug);
}
