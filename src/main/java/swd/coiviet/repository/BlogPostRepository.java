package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.BlogPost;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlug(String slug);
    List<BlogPost> findByProvinceId(Long provinceId);
    List<BlogPost> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status);
    List<BlogPost> findByTitleContainingIgnoreCase(String title);
    long countByStatus(PublicationStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
