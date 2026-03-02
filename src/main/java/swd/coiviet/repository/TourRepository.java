package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.Status;
import swd.coiviet.model.Tour;

import java.time.LocalDateTime;
import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByProvinceId(Long provinceId);
    List<Tour> findByArtisanId(Long artisanId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(Status status);

    @Query("SELECT t FROM Tour t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR (t.description IS NOT NULL AND LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Tour> findByTitleContainingOrDescriptionContainingIgnoreCase(@Param("keyword") String keyword);
}
