package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.BlogPost;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlug(String slug);
    long countByStatus(PublicationStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
