package swd.coiviet.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.Quiz;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByModuleId(Long moduleId);
    Optional<Quiz> findByModuleIdAndStatus(Long moduleId, PublicationStatus status);

    @EntityGraph(attributePaths = {"questions"})
    @Query("SELECT q FROM Quiz q WHERE q.id = :id")
    Optional<Quiz> findWithQuestionsById(@Param("id") Long id);
}
