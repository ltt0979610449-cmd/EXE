package swd.coiviet.repository;

import swd.coiviet.enums.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.UserMemory;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {
    List<UserMemory> findByUserId(Long userId);
    List<UserMemory> findByProvinceId(Long provinceId);
    List<UserMemory> findByStatus(PublicationStatus status);
    List<UserMemory> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status);
    List<UserMemory> findByUserIdAndStatus(Long userId, PublicationStatus status);
    long countByStatus(PublicationStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
