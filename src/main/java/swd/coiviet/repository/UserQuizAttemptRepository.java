package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.UserQuizAttempt;

import java.util.List;
import java.util.Optional;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
    List<UserQuizAttempt> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<UserQuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    Optional<UserQuizAttempt> findByIdAndUserId(Long id, Long userId);
}
