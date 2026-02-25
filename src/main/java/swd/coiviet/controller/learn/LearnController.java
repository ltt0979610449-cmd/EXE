package swd.coiviet.controller.learn;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.dto.response.*;
import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.*;
import swd.coiviet.service.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/learn")
public class LearnController {

    private final LearnCategoryService categoryService;
    private final LearnModuleService moduleService;
    private final LearnLessonService lessonService;
    private final QuizService quizService;

    public LearnController(LearnCategoryService categoryService, LearnModuleService moduleService,
                           LearnLessonService lessonService, QuizService quizService) {
        this.categoryService = categoryService;
        this.moduleService = moduleService;
        this.lessonService = lessonService;
        this.quizService = quizService;
    }

    @GetMapping("/public/categories")
    @Operation(summary = "Danh sách category Learn")
    public ResponseEntity<swd.coiviet.dto.response.ApiResponse<List<LearnCategoryResponse>>> getCategories() {
        List<LearnCategory> categories = categoryService.findAllActive();
        List<LearnCategoryResponse> responses = categories.stream()
                .map(c -> LearnCategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .orderIndex(c.getOrderIndex())
                        .build())
                .toList();
        return ResponseEntity.ok(swd.coiviet.dto.response.ApiResponse.success(responses));
    }

    @GetMapping("/public/modules")
    @Operation(summary = "Danh sách module Learn")
    public ResponseEntity<swd.coiviet.dto.response.ApiResponse<List<LearnModuleResponse>>> getModules(
            @RequestParam(required = false) Long categoryId) {
        List<LearnModule> modules = categoryId != null
                ? moduleService.findByCategoryIdAndStatus(categoryId, LearnModuleStatus.PUBLISHED)
                : moduleService.findByStatus(LearnModuleStatus.PUBLISHED);
        List<LearnModuleResponse> responses = modules.stream()
                .map(this::toModuleListResponse)
                .toList();
        return ResponseEntity.ok(swd.coiviet.dto.response.ApiResponse.success(responses));
    }

    @GetMapping("/public/modules/{id}")
    @Operation(summary = "Chi tiết module")
    public ResponseEntity<swd.coiviet.dto.response.ApiResponse<LearnModuleResponse>> getModuleById(@PathVariable Long id) {
        LearnModule module = moduleService.findByIdWithRelations(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        if (module.getStatus() != LearnModuleStatus.PUBLISHED) {
            throw new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại");
        }
        return ResponseEntity.ok(swd.coiviet.dto.response.ApiResponse.success(toModuleDetailResponse(module)));
    }

    @GetMapping("/public/lessons/{id}")
    @Operation(summary = "Chi tiết bài học")
    public ResponseEntity<swd.coiviet.dto.response.ApiResponse<LearnLessonResponse>> getLessonById(@PathVariable Long id) {
        LearnLesson lesson = lessonService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bài học không tồn tại"));
        if (lesson.getStatus() != PublicationStatus.PUBLISHED) {
            throw new AppException(ErrorCode.NOT_FOUND, "Bài học không tồn tại");
        }
        lessonService.incrementViews(id);
        return ResponseEntity.ok(swd.coiviet.dto.response.ApiResponse.success(toLessonResponse(lesson)));
    }

    @GetMapping("/public/quizzes/{id}")
    @Operation(summary = "Lấy đề quiz (không gửi đáp án đúng)")
    @Transactional(readOnly = true)
    public ResponseEntity<swd.coiviet.dto.response.ApiResponse<QuizResponse>> getQuizById(@PathVariable Long id) {
        Quiz quiz = quizService.findByIdWithQuestions(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        if (quiz.getStatus() != PublicationStatus.PUBLISHED) {
            throw new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại");
        }
        return ResponseEntity.ok(swd.coiviet.dto.response.ApiResponse.success(toQuizResponse(quiz, false)));
    }

    private LearnModuleResponse toModuleListResponse(LearnModule m) {
        List<LearnLesson> lessons = lessonService.findByModuleIdAndStatus(m.getId(), PublicationStatus.PUBLISHED);
        int durationMinutes = lessons.stream()
                .mapToInt(l -> l.getEstimatedMinutes() != null ? l.getEstimatedMinutes() : 0)
                .sum();
        return LearnModuleResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .slug(m.getSlug())
                .thumbnailUrl(m.getThumbnailUrl())
                .categoryId(m.getCategory() != null ? m.getCategory().getId() : null)
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                .lessonsCount(lessons.size())
                .durationMinutes(durationMinutes)
                .build();
    }

    private LearnModuleResponse toModuleDetailResponse(LearnModule m) {
        List<LearnLesson> lessons = lessonService.findByModuleIdAndStatus(m.getId(), PublicationStatus.PUBLISHED);
        int durationMinutes = lessons.stream()
                .mapToInt(l -> l.getEstimatedMinutes() != null ? l.getEstimatedMinutes() : 0)
                .sum();
        List<LearnLessonSummaryResponse> lessonSummaries = lessons.stream()
                .map(l -> LearnLessonSummaryResponse.builder()
                        .id(l.getId())
                        .title(l.getTitle())
                        .slug(l.getSlug())
                        .thumbnailUrl(l.getImageUrl())
                        .duration(l.getEstimatedMinutes())
                        .videoUrl(l.getVideoUrl())
                        .orderIndex(l.getOrderIndex())
                        .build())
                .toList();
        QuizSummaryResponse quizPrompt = null;
        if (m.getQuiz() != null && m.getQuiz().getStatus() == PublicationStatus.PUBLISHED) {
            quizPrompt = QuizSummaryResponse.builder()
                    .id(m.getQuiz().getId())
                    .title(m.getQuiz().getTitle())
                    .totalQuestions(m.getQuiz().getQuestions() != null ? m.getQuiz().getQuestions().size() : 0)
                    .timeLimitMinutes(m.getQuiz().getTimeLimitMinutes())
                    .build();
        }
        List<TourSummaryResponse> suggestedTours = new ArrayList<>();
        if (m.getSuggestedTours() != null) {
            for (Tour t : m.getSuggestedTours()) {
                suggestedTours.add(TourSummaryResponse.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .slug(t.getSlug())
                        .thumbnailUrl(t.getThumbnailUrl())
                        .location(t.getProvince() != null ? t.getProvince().getName() : null)
                        .description(t.getDescription())
                        .price(t.getPrice())
                        .build());
            }
        }
        return LearnModuleResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .slug(m.getSlug())
                .thumbnailUrl(m.getThumbnailUrl())
                .categoryId(m.getCategory() != null ? m.getCategory().getId() : null)
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                .quickNotesJson(m.getQuickNotesJson())
                .culturalEtiquetteTitle(m.getCulturalEtiquetteTitle())
                .culturalEtiquetteText(m.getCulturalEtiquetteText())
                .lessonsCount(lessons.size())
                .durationMinutes(durationMinutes)
                .lessons(lessonSummaries)
                .quizPrompt(quizPrompt)
                .suggestedTours(suggestedTours)
                .build();
    }

    private LearnLessonResponse toLessonResponse(LearnLesson l) {
        return LearnLessonResponse.builder()
                .id(l.getId())
                .title(l.getTitle())
                .slug(l.getSlug())
                .imageUrl(l.getImageUrl())
                .contentJson(l.getContentJson())
                .vocabularyJson(l.getVocabularyJson())
                .objectiveText(l.getObjectiveText())
                .difficulty(l.getDifficulty())
                .estimatedMinutes(l.getEstimatedMinutes())
                .videoUrl(l.getVideoUrl())
                .viewsCount(l.getViewsCount())
                .orderIndex(l.getOrderIndex())
                .totalLessonsInModule((long) lessonService.findByModuleIdAndStatus(l.getModule().getId(), PublicationStatus.PUBLISHED).size())
                .author(l.getArtisan() != null ? ArtisanSummaryResponse.builder()
                        .id(l.getArtisan().getId())
                        .fullName(l.getArtisan().getFullName())
                        .profileImageUrl(l.getArtisan().getProfileImageUrl())
                        .build() : null)
                .moduleId(l.getModule().getId())
                .moduleTitle(l.getModule().getTitle())
                .categoryName(l.getModule().getCategory() != null ? l.getModule().getCategory().getName() : null)
                .build();
    }

    private QuizResponse toQuizResponse(Quiz q, boolean includeCorrectAnswer) {
        List<String> rules = new ArrayList<>();
        if (q.getRulesJson() != null && !q.getRulesJson().isEmpty()) {
            try {
                rules = java.util.Arrays.asList(q.getRulesJson().split("\\|"));
            } catch (Exception ignored) {
            }
        }
        List<QuizQuestionResponse> questionResponses = new ArrayList<>();
        if (q.getQuestions() != null) {
            for (QuizQuestion qq : q.getQuestions()) {
                List<QuizOptionResponse> optionResponses = qq.getOptions().stream()
                        .map(o -> QuizOptionResponse.builder()
                                .id(o.getId())
                                .label(o.getLabel())
                                .optionText(o.getOptionText())
                                .isCorrect(includeCorrectAnswer ? o.getIsCorrect() : null)
                                .build())
                        .toList();
                questionResponses.add(QuizQuestionResponse.builder()
                        .id(qq.getId())
                        .questionText(qq.getQuestionText())
                        .hintText(qq.getHintText())
                        .orderIndex(qq.getOrderIndex())
                        .options(optionResponses)
                        .build());
            }
        }
        return QuizResponse.builder()
                .id(q.getId())
                .moduleId(q.getModule() != null ? q.getModule().getId() : null)
                .title(q.getTitle())
                .timeLimitMinutes(q.getTimeLimitMinutes())
                .difficulty(q.getDifficulty())
                .objective(q.getObjective())
                .rules(rules)
                .totalQuestions(q.getQuestions() != null ? q.getQuestions().size() : 0)
                .questions(questionResponses)
                .build();
    }
}
