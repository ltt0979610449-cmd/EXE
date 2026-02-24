package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.Quiz;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByModuleId(Long moduleId);
    Optional<Quiz> findByModuleIdAndStatus(Long moduleId, PublicationStatus status);
}
