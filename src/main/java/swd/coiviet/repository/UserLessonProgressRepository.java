package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.UserLessonProgress;

import java.util.List;
import java.util.Optional;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {
    Optional<UserLessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<UserLessonProgress> findByUserId(Long userId);
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);
}
