package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.enums.Status;
import swd.coiviet.model.Tour;

import java.time.LocalDateTime;
import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByProvinceId(Long provinceId);
    List<Tour> findByArtisanId(Long artisanId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(Status status);
}
