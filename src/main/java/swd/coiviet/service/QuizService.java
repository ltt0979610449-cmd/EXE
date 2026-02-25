package swd.coiviet.service;

import swd.coiviet.dto.request.AddQuizQuestionRequest;
import swd.coiviet.model.Quiz;
import swd.coiviet.model.QuizQuestion;

import java.util.Optional;

public interface QuizService {
    Quiz save(Quiz q);
    Optional<Quiz> findById(Long id);
    Optional<Quiz> findByIdWithQuestions(Long id);
    Optional<Quiz> findByModuleId(Long moduleId);
    QuizQuestion addQuestion(Long quizId, AddQuizQuestionRequest req);
    void deleteById(Long id);
}
