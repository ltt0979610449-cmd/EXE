package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.QuizOption;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionIdOrderByLabelAsc(Long questionId);
}
