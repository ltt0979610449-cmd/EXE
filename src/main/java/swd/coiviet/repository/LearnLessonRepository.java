package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.LearnLesson;

import java.util.List;
import java.util.Optional;

public interface LearnLessonRepository extends JpaRepository<LearnLesson, Long> {
    List<LearnLesson> findByModuleIdOrderByOrderIndexAsc(Long moduleId);
    List<LearnLesson> findByModuleIdAndStatusOrderByOrderIndexAsc(Long moduleId, PublicationStatus status);
    List<LearnLesson> findAllByStatusOrderByOrderIndexAsc(PublicationStatus status);
    Optional<LearnLesson> findBySlug(String slug);
}
