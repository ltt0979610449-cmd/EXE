package swd.coiviet.controller.learn;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.AddQuizQuestionBulkRequest;
import swd.coiviet.dto.request.AddQuizQuestionRequest;
import swd.coiviet.dto.response.*;
import swd.coiviet.enums.LearnDifficulty;
import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.*;
import swd.coiviet.service.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/learn")
@Tag(name = "Learn", description = "Quản lý Learn (Staff/Admin)")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class LearnStaffController {

    private final LearnCategoryService categoryService;
    private final LearnModuleService moduleService;
    private final LearnLessonService lessonService;
    private final QuizService quizService;
    private final CloudinaryService cloudinaryService;
    private final ProvinceService provinceService;
    private final ArtisanService artisanService;
    private final TourService tourService;
    private final VoucherService voucherService;

    public LearnStaffController(LearnCategoryService categoryService, LearnModuleService moduleService,
                               LearnLessonService lessonService, QuizService quizService,
                               CloudinaryService cloudinaryService, ProvinceService provinceService,
                               ArtisanService artisanService, TourService tourService,
                               VoucherService voucherService) {
        this.categoryService = categoryService;
        this.moduleService = moduleService;
        this.lessonService = lessonService;
        this.quizService = quizService;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
        this.artisanService = artisanService;
        this.tourService = tourService;
        this.voucherService = voucherService;
    }

    @PostMapping(value = "/modules", consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo module Learn")
    @Transactional
    public ResponseEntity<ApiResponse<LearnModuleResponse>> createModule(
            @RequestParam @NotNull Long categoryId,
            @RequestParam @NotBlank String title,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String quickNotesJson,
            @RequestParam(required = false) String culturalEtiquetteTitle,
            @RequestParam(required = false) String culturalEtiquetteText,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Integer orderIndex,
            @RequestParam(required = false) Long[] tourIds,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        try {
            LearnCategory category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Category không tồn tại"));
            LearnModule module = LearnModule.builder()
                    .category(category)
                    .title(title)
                    .slug(slug)
                    .quickNotesJson(quickNotesJson)
                    .culturalEtiquetteTitle(culturalEtiquetteTitle)
                    .culturalEtiquetteText(culturalEtiquetteText)
                    .status(LearnModuleStatus.DRAFT)
                    .orderIndex(orderIndex != null ? orderIndex : 0)
                    .createdAt(LocalDateTime.now())
                    .build();
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh không tồn tại"));
                module.setProvince(province);
            }
            LearnModule saved = moduleService.save(module);
            if (tourIds != null && tourIds.length > 0) {
                List<Tour> tours = new ArrayList<>();
                for (Long tid : tourIds) {
                    tourService.findById(tid).ifPresent(tours::add);
                }
                saved.setSuggestedTours(tours);
                saved = moduleService.save(saved);
            }
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
                String url = cloudinaryService.uploadLearnModuleThumbnail(thumbnail, saved.getId());
                saved.setThumbnailUrl(url);
                saved = moduleService.save(saved);
            }
            LearnModule withRelations = moduleService.findByIdWithRelations(saved.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
            return ResponseEntity.ok(ApiResponse.success(toModuleResponse(withRelations), "Tạo module thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/modules/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật module")
    @Transactional
    public ResponseEntity<ApiResponse<LearnModuleResponse>> updateModule(
            @PathVariable Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String quickNotesJson,
            @RequestParam(required = false) String culturalEtiquetteTitle,
            @RequestParam(required = false) String culturalEtiquetteText,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Integer orderIndex,
            @RequestParam(required = false) Long[] tourIds,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        LearnModule existing = moduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        try {
            if (categoryId != null) {
                LearnCategory cat = categoryService.findById(categoryId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Category không tồn tại"));
                existing.setCategory(cat);
            }
            if (title != null) existing.setTitle(title);
            if (slug != null) existing.setSlug(slug);
            if (quickNotesJson != null) existing.setQuickNotesJson(quickNotesJson);
            if (culturalEtiquetteTitle != null) existing.setCulturalEtiquetteTitle(culturalEtiquetteTitle);
            if (culturalEtiquetteText != null) existing.setCulturalEtiquetteText(culturalEtiquetteText);
            if (provinceId != null) {
                Province p = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh không tồn tại"));
                existing.setProvince(p);
            }
            if (orderIndex != null) existing.setOrderIndex(orderIndex);
            if (tourIds != null) {
                List<Tour> tours = new ArrayList<>();
                for (Long tid : tourIds) {
                    tourService.findById(tid).ifPresent(tours::add);
                }
                existing.setSuggestedTours(tours);
            }
            if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.getSize() > 0) {
                String url = cloudinaryService.uploadLearnModuleThumbnail(thumbnail, id);
                existing.setThumbnailUrl(url);
            }
            moduleService.save(existing);
            LearnModule withRelations = moduleService.findByIdWithRelations(id)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
            return ResponseEntity.ok(ApiResponse.success(toModuleResponse(withRelations), "Cập nhật module thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/modules/{id}/publish")
    @Operation(summary = "Publish module")
    @Transactional
    public ResponseEntity<ApiResponse<LearnModuleResponse>> publishModule(@PathVariable Long id) {
        LearnModule m = moduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        m.setStatus(LearnModuleStatus.PUBLISHED);
        moduleService.save(m);
        LearnModule withRelations = moduleService.findByIdWithRelations(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(toModuleResponse(withRelations), "Publish module thành công"));
    }

    @PutMapping("/modules/{id}/status")
    @Operation(summary = "Điều chỉnh trạng thái module", description = "Đặt trạng thái: DRAFT, PUBLISHED")
    @Transactional
    public ResponseEntity<ApiResponse<LearnModuleResponse>> updateModuleStatus(
            @PathVariable Long id,
            @RequestParam LearnModuleStatus status) {
        LearnModule m = moduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        m.setStatus(status);
        moduleService.save(m);
        LearnModule withRelations = moduleService.findByIdWithRelations(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(toModuleResponse(withRelations), "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/modules/{id}")
    @Operation(summary = "Xóa module")
    public ResponseEntity<ApiResponse<Void>> deleteModule(@PathVariable Long id) {
        moduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        moduleService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa module thành công"));
    }

    @GetMapping("/lessons")
    @Operation(summary = "Danh sách tất cả lesson")
    public ResponseEntity<ApiResponse<List<LearnLessonResponse>>> getAllLessons() {
        List<LearnLesson> lessons = lessonService.findAll();
        List<LearnLessonResponse> responses = lessons.stream()
                .map(this::toLessonResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping(value = "/lessons", consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo lesson")
    public ResponseEntity<ApiResponse<LearnLessonResponse>> createLesson(
            @RequestParam @NotNull Long moduleId,
            @RequestParam @NotBlank String title,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) Long artisanId,
            @RequestParam(required = false) String contentJson,
            @RequestParam(required = false) String vocabularyJson,
            @RequestParam(required = false) String objectiveText,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer estimatedMinutes,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) Integer orderIndex,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            LearnModule module = moduleService.findById(moduleId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
            LearnLesson lesson = LearnLesson.builder()
                    .module(module)
                    .title(title)
                    .slug(slug)
                    .contentJson(contentJson)
                    .vocabularyJson(vocabularyJson)
                    .objectiveText(objectiveText)
                    .difficulty(difficulty != null ? LearnDifficulty.valueOf(difficulty) : null)
                    .estimatedMinutes(estimatedMinutes)
                    .videoUrl(videoUrl)
                    .orderIndex(orderIndex != null ? orderIndex : 0)
                    .viewsCount(0)
                    .status(PublicationStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .build();
            if (artisanId != null) {
                Artisan artisan = artisanService.findById(artisanId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
                lesson.setArtisan(artisan);
            }
            LearnLesson saved = lessonService.save(lesson);
            if (image != null && !image.isEmpty() && image.getSize() > 0) {
                String url = cloudinaryService.uploadLearnLessonImage(image, saved.getId());
                saved.setImageUrl(url);
                saved = lessonService.save(saved);
            }
            return ResponseEntity.ok(ApiResponse.success(toLessonResponse(saved), "Tạo lesson thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/lessons/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật lesson")
    @Transactional
    public ResponseEntity<ApiResponse<LearnLessonResponse>> updateLesson(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) Long artisanId,
            @RequestParam(required = false) String contentJson,
            @RequestParam(required = false) String vocabularyJson,
            @RequestParam(required = false) String objectiveText,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer estimatedMinutes,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) Integer orderIndex,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        LearnLesson existing = lessonService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lesson không tồn tại"));
        try {
            if (title != null) existing.setTitle(title);
            if (slug != null) existing.setSlug(slug);
            if (contentJson != null) existing.setContentJson(contentJson);
            if (vocabularyJson != null) existing.setVocabularyJson(vocabularyJson);
            if (objectiveText != null) existing.setObjectiveText(objectiveText);
            if (difficulty != null) existing.setDifficulty(LearnDifficulty.valueOf(difficulty));
            if (estimatedMinutes != null) existing.setEstimatedMinutes(estimatedMinutes);
            if (videoUrl != null) existing.setVideoUrl(videoUrl);
            if (orderIndex != null) existing.setOrderIndex(orderIndex);
            if (artisanId != null) {
                Artisan artisan = artisanService.findById(artisanId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));
                existing.setArtisan(artisan);
            }
            if (image != null && !image.isEmpty() && image.getSize() > 0) {
                String url = cloudinaryService.uploadLearnLessonImage(image, id);
                existing.setImageUrl(url);
            }
            LearnLesson saved = lessonService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(toLessonResponse(saved), "Cập nhật lesson thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/lessons/{id}/publish")
    @Operation(summary = "Publish lesson")
    @Transactional
    public ResponseEntity<ApiResponse<LearnLessonResponse>> publishLesson(@PathVariable Long id) {
        LearnLesson l = lessonService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lesson không tồn tại"));
        l.setStatus(PublicationStatus.PUBLISHED);
        LearnLesson saved = lessonService.save(l);
        return ResponseEntity.ok(ApiResponse.success(toLessonResponse(saved), "Publish lesson thành công"));
    }

    @PutMapping("/lessons/{id}/status")
    @Operation(summary = "Điều chỉnh trạng thái lesson", description = "Đặt trạng thái: DRAFT, PUBLISHED, ARCHIVED")
    @Transactional
    public ResponseEntity<ApiResponse<LearnLessonResponse>> updateLessonStatus(
            @PathVariable Long id,
            @RequestParam PublicationStatus status) {
        LearnLesson l = lessonService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lesson không tồn tại"));
        l.setStatus(status);
        LearnLesson saved = lessonService.save(l);
        return ResponseEntity.ok(ApiResponse.success(toLessonResponse(saved), "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/lessons/{id}")
    @Operation(summary = "Xóa lesson")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long id) {
        lessonService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lesson không tồn tại"));
        lessonService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa lesson thành công"));
    }

    @PostMapping("/quizzes")
    @Operation(summary = "Tạo quiz")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @RequestParam @NotNull Long moduleId,
            @RequestParam @NotBlank String title,
            @RequestParam(required = false) Integer timeLimitMinutes,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String objective,
            @RequestParam(required = false) String rulesJson,
            @RequestParam(required = false) Long achievementVoucherId) {
        LearnModule module = moduleService.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        Quiz quiz = Quiz.builder()
                .module(module)
                .title(title)
                .timeLimitMinutes(timeLimitMinutes != null ? timeLimitMinutes : 5)
                .difficulty(difficulty != null ? LearnDifficulty.valueOf(difficulty) : LearnDifficulty.BASIC)
                .objective(objective)
                .rulesJson(rulesJson)
                .status(PublicationStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();
        if (achievementVoucherId != null) {
            Voucher v = voucherService.findById(achievementVoucherId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
            quiz.setAchievementVoucher(v);
        }
        Quiz saved = quizService.save(quiz);
        return ResponseEntity.ok(ApiResponse.success(toQuizResponse(saved), "Tạo quiz thành công"));
    }

    @PutMapping("/quizzes/{id}")
    @Operation(summary = "Cập nhật quiz")
    @Transactional
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer timeLimitMinutes,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String objective,
            @RequestParam(required = false) String rulesJson,
            @RequestParam(required = false) Long achievementVoucherId) {
        Quiz existing = quizService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        if (title != null) existing.setTitle(title);
        if (timeLimitMinutes != null) existing.setTimeLimitMinutes(timeLimitMinutes);
        if (difficulty != null) existing.setDifficulty(LearnDifficulty.valueOf(difficulty));
        if (objective != null) existing.setObjective(objective);
        if (rulesJson != null) existing.setRulesJson(rulesJson);
        if (achievementVoucherId != null) {
            Voucher v = voucherService.findById(achievementVoucherId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
            existing.setAchievementVoucher(v);
        }
        quizService.save(existing);
        Quiz updated = quizService.findByIdWithQuestions(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(toQuizResponse(updated), "Cập nhật quiz thành công"));
    }

    @PutMapping("/quizzes/{id}/publish")
    @Operation(summary = "Publish quiz")
    @Transactional
    public ResponseEntity<ApiResponse<QuizResponse>> publishQuiz(@PathVariable Long id) {
        Quiz q = quizService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        q.setStatus(PublicationStatus.PUBLISHED);
        quizService.save(q);
        Quiz updated = quizService.findByIdWithQuestions(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(toQuizResponse(updated), "Publish quiz thành công"));
    }

    @PutMapping("/quizzes/{id}/status")
    @Operation(summary = "Điều chỉnh trạng thái quiz", description = "Đặt trạng thái: DRAFT, PUBLISHED, ARCHIVED")
    @Transactional
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuizStatus(
            @PathVariable Long id,
            @RequestParam PublicationStatus status) {
        Quiz q = quizService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        q.setStatus(status);
        quizService.save(q);
        Quiz updated = quizService.findByIdWithQuestions(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success(toQuizResponse(updated), "Cập nhật trạng thái thành công"));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @Operation(summary = "Thêm câu hỏi vào quiz")
    public ResponseEntity<ApiResponse<QuizQuestionResponse>> addQuizQuestion(
            @PathVariable Long quizId,
            @RequestBody AddQuizQuestionRequest req) {
        QuizQuestion q = quizService.addQuestion(quizId, req);
        return ResponseEntity.ok(ApiResponse.success(toQuizQuestionResponse(q), "Thêm câu hỏi thành công"));
    }

    @PostMapping("/quizzes/{quizId}/questions/bulk")
    @Operation(summary = "Thêm nhiều câu hỏi vào quiz (bulk)")
    public ResponseEntity<ApiResponse<List<QuizQuestionResponse>>> addQuizQuestionsBulk(
            @PathVariable Long quizId,
            @RequestBody @Valid AddQuizQuestionBulkRequest req) {
        List<QuizQuestion> added = quizService.addQuestionsBulk(quizId, req);
        List<QuizQuestionResponse> responses = added.stream()
                .map(this::toQuizQuestionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Đã thêm " + added.size() + " câu hỏi thành công"));
    }

    @DeleteMapping("/quizzes/{id}")
    @Operation(summary = "Xóa quiz")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        quizService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        quizService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa quiz thành công"));
    }

    private LearnModuleResponse toModuleResponse(LearnModule m) {
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
        if (m.getQuiz() != null) {
            int totalQuestions = m.getQuiz().getQuestions() != null ? m.getQuiz().getQuestions().size() : 0;
            quizPrompt = QuizSummaryResponse.builder()
                    .id(m.getQuiz().getId())
                    .title(m.getQuiz().getTitle())
                    .totalQuestions(totalQuestions)
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
                .viewsCount(l.getViewsCount() != null ? l.getViewsCount() : 0)
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

    private QuizQuestionResponse toQuizQuestionResponse(QuizQuestion qq) {
        List<QuizOptionResponse> optionResponses = qq.getOptions() != null
                ? qq.getOptions().stream()
                        .map(o -> QuizOptionResponse.builder()
                                .id(o.getId())
                                .label(o.getLabel())
                                .optionText(o.getOptionText())
                                .isCorrect(o.getIsCorrect())
                                .build())
                        .toList()
                : new ArrayList<>();
        return QuizQuestionResponse.builder()
                .id(qq.getId())
                .questionText(qq.getQuestionText())
                .hintText(qq.getHintText())
                .orderIndex(qq.getOrderIndex())
                .options(optionResponses)
                .build();
    }

    private QuizResponse toQuizResponse(Quiz q) {
        List<String> rules = new ArrayList<>();
        if (q.getRulesJson() != null && !q.getRulesJson().isEmpty()) {
            try {
                rules = Arrays.asList(q.getRulesJson().split("\\|"));
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
                                .isCorrect(o.getIsCorrect())
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
