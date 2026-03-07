package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.model.QuizQuestion;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByOrderIndexAsc(Long quizId);

    @Query("SELECT DISTINCT q FROM QuizQuestion q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId ORDER BY q.orderIndex ASC")
    List<QuizQuestion> findByQuizIdWithOptions(@Param("quizId") Long quizId);
}
