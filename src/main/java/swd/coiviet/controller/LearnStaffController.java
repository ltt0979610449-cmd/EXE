package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.dto.request.AddQuizQuestionRequest;
import swd.coiviet.dto.response.ApiResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/learn")
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
    public ResponseEntity<ApiResponse<LearnModule>> createModule(
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
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo module thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/modules/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật module")
    public ResponseEntity<ApiResponse<LearnModule>> updateModule(
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
            return ResponseEntity.ok(ApiResponse.success(moduleService.save(existing), "Cập nhật module thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/modules/{id}/publish")
    @Operation(summary = "Publish module")
    public ResponseEntity<ApiResponse<LearnModule>> publishModule(@PathVariable Long id) {
        LearnModule m = moduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        m.setStatus(LearnModuleStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(moduleService.save(m), "Publish module thành công"));
    }

    @DeleteMapping("/modules/{id}")
    @Operation(summary = "Xóa module")
    public ResponseEntity<ApiResponse<Void>> deleteModule(@PathVariable Long id) {
        moduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Module không tồn tại"));
        moduleService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa module thành công"));
    }

    @PostMapping("/lessons")
    @Operation(summary = "Tạo lesson")
    public ResponseEntity<ApiResponse<LearnLesson>> createLesson(
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
            return ResponseEntity.ok(ApiResponse.success(saved, "Tạo lesson thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping(value = "/lessons/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật lesson")
    public ResponseEntity<ApiResponse<LearnLesson>> updateLesson(
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
            return ResponseEntity.ok(ApiResponse.success(lessonService.save(existing), "Cập nhật lesson thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/lessons/{id}/publish")
    @Operation(summary = "Publish lesson")
    public ResponseEntity<ApiResponse<LearnLesson>> publishLesson(@PathVariable Long id) {
        LearnLesson l = lessonService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lesson không tồn tại"));
        l.setStatus(PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(lessonService.save(l), "Publish lesson thành công"));
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
    public ResponseEntity<ApiResponse<Quiz>> createQuiz(
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
        return ResponseEntity.ok(ApiResponse.success(quizService.save(quiz), "Tạo quiz thành công"));
    }

    @PutMapping("/quizzes/{id}")
    @Operation(summary = "Cập nhật quiz")
    public ResponseEntity<ApiResponse<Quiz>> updateQuiz(
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
        return ResponseEntity.ok(ApiResponse.success(quizService.save(existing), "Cập nhật quiz thành công"));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @Operation(summary = "Thêm câu hỏi vào quiz")
    public ResponseEntity<ApiResponse<QuizQuestion>> addQuizQuestion(
            @PathVariable Long quizId,
            @RequestBody AddQuizQuestionRequest req) {
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        QuizQuestion q = QuizQuestion.builder()
                .quiz(quiz)
                .questionText(req.getQuestionText())
                .hintText(req.getHintText())
                .explanationText(req.getExplanationText())
                .orderIndex(req.getOrderIndex() != null ? req.getOrderIndex() : quiz.getQuestions().size())
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
        quizService.save(quiz);
        return ResponseEntity.ok(ApiResponse.success(q, "Thêm câu hỏi thành công"));
    }

    @DeleteMapping("/quizzes/{id}")
    @Operation(summary = "Xóa quiz")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        quizService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        quizService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa quiz thành công"));
    }
}
