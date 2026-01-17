package swd.coiviet.repository;

import swd.coiviet.enums.CultureCategory;
import swd.coiviet.enums.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.CultureItem;

import java.time.LocalDateTime;
import java.util.List;

public interface CultureItemRepository extends JpaRepository<CultureItem, Long> {
    List<CultureItem> findByProvinceId(Long provinceId);
    List<CultureItem> findByCategory(CultureCategory category);
    List<CultureItem> findByProvinceIdAndCategory(Long provinceId, CultureCategory category);
    List<CultureItem> findByStatus(PublicationStatus status);
    List<CultureItem> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status);
    long countByStatus(PublicationStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
