package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.UserLessonSave;

import java.util.List;
import java.util.Optional;

public interface UserLessonSaveRepository extends JpaRepository<UserLessonSave, Long> {
    Optional<UserLessonSave> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<UserLessonSave> findByUserId(Long userId);
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);
}
