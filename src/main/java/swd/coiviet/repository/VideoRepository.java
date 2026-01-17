package swd.coiviet.repository;

import swd.coiviet.enums.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.Video;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByProvinceId(Long provinceId);
    List<Video> findByStatus(PublicationStatus status);
    List<Video> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status);
    long countByStatus(PublicationStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
