package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.UserLessonLike;

import java.util.List;
import java.util.Optional;

public interface UserLessonLikeRepository extends JpaRepository<UserLessonLike, Long> {
    Optional<UserLessonLike> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<UserLessonLike> findByUserId(Long userId);
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);
}
