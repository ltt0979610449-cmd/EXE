package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.dto.request.AddQuizQuestionRequest;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Quiz;
import swd.coiviet.model.QuizOption;
import swd.coiviet.model.QuizQuestion;
import swd.coiviet.repository.QuizRepository;
import swd.coiviet.service.QuizService;

import java.util.Optional;

@Service
public class QuizServiceImpl implements QuizService {
    private final QuizRepository repo;

    public QuizServiceImpl(QuizRepository repo) {
        this.repo = repo;
    }

    @Override
    public Quiz save(Quiz q) {
        return repo.save(q);
    }

    @Override
    public Optional<Quiz> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<Quiz> findByIdWithQuestions(Long id) {
        return repo.findWithQuestionsById(id);
    }

    @Override
    public Optional<Quiz> findByModuleId(Long moduleId) {
        return repo.findByModuleId(moduleId);
    }

    @Override
    @Transactional
    public QuizQuestion addQuestion(Long quizId, AddQuizQuestionRequest req) {
        Quiz quiz = repo.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        int orderIndex = req.getOrderIndex() != null ? req.getOrderIndex() : quiz.getQuestions().size();
        QuizQuestion q = QuizQuestion.builder()
                .quiz(quiz)
                .questionText(req.getQuestionText())
                .hintText(req.getHintText())
                .explanationText(req.getExplanationText())
                .orderIndex(orderIndex)
                .build();
        if (req.getOptions() != null) {
            for (AddQuizQuestionRequest.QuizOptionInput opt : req.getOptions()) {
                q.getOptions().add(QuizOption.builder()
                        .question(q)
                        .label(opt.getLabel())
                        .optionText(opt.getOptionText())
                        .isCorrect(opt.getIsCorrect() != null ? opt.getIsCorrect() : false)
                        .build());
            }
        }
        quiz.getQuestions().add(q);
        repo.save(quiz);
        return q;
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
