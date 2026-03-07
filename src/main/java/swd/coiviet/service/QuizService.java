package swd.coiviet.service;

import swd.coiviet.dto.request.AddQuizQuestionBulkRequest;
import swd.coiviet.dto.request.AddQuizQuestionRequest;
import swd.coiviet.model.Quiz;
import swd.coiviet.model.QuizQuestion;

import java.util.List;
import java.util.Optional;

public interface QuizService {
    Quiz save(Quiz q);
    Optional<Quiz> findById(Long id);
    Optional<Quiz> findByIdWithQuestions(Long id);
    Optional<Quiz> findByModuleId(Long moduleId);
    QuizQuestion addQuestion(Long quizId, AddQuizQuestionRequest req);
    List<QuizQuestion> addQuestionsBulk(Long quizId, AddQuizQuestionBulkRequest req);
    Optional<Quiz> findByIdWithQuestionsAndModule(Long id);
    void deleteById(Long id);
}
