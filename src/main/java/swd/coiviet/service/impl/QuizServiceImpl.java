package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.dto.request.AddQuizQuestionBulkRequest;
import swd.coiviet.dto.request.AddQuizQuestionRequest;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Quiz;
import swd.coiviet.model.QuizOption;
import swd.coiviet.model.QuizQuestion;
import swd.coiviet.repository.LearnModuleRepository;
import swd.coiviet.repository.QuizQuestionRepository;
import swd.coiviet.repository.QuizRepository;
import swd.coiviet.service.QuizService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuizServiceImpl implements QuizService {
    private final QuizRepository repo;
    private final QuizQuestionRepository quizQuestionRepo;
    private final LearnModuleRepository learnModuleRepo;

    public QuizServiceImpl(QuizRepository repo, QuizQuestionRepository quizQuestionRepo,
                          LearnModuleRepository learnModuleRepo) {
        this.repo = repo;
        this.quizQuestionRepo = quizQuestionRepo;
        this.learnModuleRepo = learnModuleRepo;
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
        Optional<Quiz> opt = repo.findWithQuestionsById(id);
        if (opt.isPresent()) {
            quizQuestionRepo.findByQuizIdWithOptions(id);
            return opt;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Quiz> findByIdWithQuestionsAndModule(Long id) {
        Optional<Quiz> opt = findByIdWithQuestions(id);
        if (opt.isPresent() && opt.get().getModule() != null) {
            learnModuleRepo.findWithToursById(opt.get().getModule().getId());
        }
        return opt;
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
    @Transactional
    public List<QuizQuestion> addQuestionsBulk(Long quizId, AddQuizQuestionBulkRequest req) {
        Quiz quiz = repo.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        if (req.getQuestions() == null || req.getQuestions().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Danh sách câu hỏi không được rỗng");
        }
        List<QuizQuestion> added = new ArrayList<>();
        int baseOrderIndex = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        for (int i = 0; i < req.getQuestions().size(); i++) {
            AddQuizQuestionRequest item = req.getQuestions().get(i);
            int orderIndex = item.getOrderIndex() != null ? item.getOrderIndex() : baseOrderIndex + i;
            QuizQuestion q = QuizQuestion.builder()
                    .quiz(quiz)
                    .questionText(item.getQuestionText())
                    .hintText(item.getHintText())
                    .explanationText(item.getExplanationText())
                    .orderIndex(orderIndex)
                    .build();
            if (item.getOptions() != null) {
                for (AddQuizQuestionRequest.QuizOptionInput opt : item.getOptions()) {
                    q.getOptions().add(QuizOption.builder()
                            .question(q)
                            .label(opt.getLabel())
                            .optionText(opt.getOptionText())
                            .isCorrect(opt.getIsCorrect() != null ? opt.getIsCorrect() : false)
                            .build());
                }
            }
            quiz.getQuestions().add(q);
            added.add(q);
        }
        repo.save(quiz);
        return added;
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
