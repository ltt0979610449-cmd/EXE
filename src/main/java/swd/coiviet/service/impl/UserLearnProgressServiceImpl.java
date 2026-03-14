package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.dto.response.*;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.*;
import swd.coiviet.repository.*;
import swd.coiviet.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserLearnProgressServiceImpl implements UserLearnProgressService {
    private final UserLessonProgressRepository progressRepo;
    private final UserQuizAttemptRepository attemptRepo;
    private final UserLessonLikeRepository likeRepo;
    private final UserLessonSaveRepository saveRepo;
    private final UserFollowArtisanRepository followRepo;
    private final UserVoucherRepository userVoucherRepo;
    private final QuizService quizService;
    private final LearnLessonService lessonService;
    private final LearnModuleService moduleService;
    private final VoucherService voucherService;
    private final TourService tourService;
    private final NotificationService notificationService;

    public UserLearnProgressServiceImpl(UserLessonProgressRepository progressRepo,
                                       UserQuizAttemptRepository attemptRepo,
                                       UserLessonLikeRepository likeRepo,
                                       UserLessonSaveRepository saveRepo,
                                       UserFollowArtisanRepository followRepo,
                                       UserVoucherRepository userVoucherRepo,
                                       QuizService quizService,
                                       LearnLessonService lessonService,
                                       LearnModuleService moduleService,
                                       VoucherService voucherService,
                                       TourService tourService,
                                       NotificationService notificationService) {
        this.progressRepo = progressRepo;
        this.attemptRepo = attemptRepo;
        this.likeRepo = likeRepo;
        this.saveRepo = saveRepo;
        this.followRepo = followRepo;
        this.userVoucherRepo = userVoucherRepo;
        this.quizService = quizService;
        this.lessonService = lessonService;
        this.moduleService = moduleService;
        this.voucherService = voucherService;
        this.tourService = tourService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void completeLesson(Long userId, Long lessonId) {
        LearnLesson lesson = lessonService.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bài học không tồn tại"));
        User user = new User();
        user.setId(userId);
        Optional<UserLessonProgress> existing = progressRepo.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent()) {
            UserLessonProgress p = existing.get();
            p.setProgressPercent(100);
            p.setCompletedAt(LocalDateTime.now());
            p.setLastAccessedAt(LocalDateTime.now());
            progressRepo.save(p);
        } else {
            UserLessonProgress progress = UserLessonProgress.builder()
                    .user(user)
                    .lesson(lesson)
                    .progressPercent(100)
                    .completedAt(LocalDateTime.now())
                    .lastAccessedAt(LocalDateTime.now())
                    .build();
            progressRepo.save(progress);
        }
    }

    @Override
    @Transactional
    public boolean toggleLessonLike(Long userId, Long lessonId) {
        Optional<UserLessonLike> existing = likeRepo.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent()) {
            likeRepo.delete(existing.get());
            return false;
        } else {
            LearnLesson lesson = lessonService.findById(lessonId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bài học không tồn tại"));
            User user = new User();
            user.setId(userId);
            likeRepo.save(UserLessonLike.builder()
                    .user(user)
                    .lesson(lesson)
                    .createdAt(LocalDateTime.now())
                    .build());
            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleLessonSave(Long userId, Long lessonId) {
        Optional<UserLessonSave> existing = saveRepo.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent()) {
            saveRepo.delete(existing.get());
            return false;
        } else {
            LearnLesson lesson = lessonService.findById(lessonId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bài học không tồn tại"));
            User user = new User();
            user.setId(userId);
            saveRepo.save(UserLessonSave.builder()
                    .user(user)
                    .lesson(lesson)
                    .createdAt(LocalDateTime.now())
                    .build());
            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleFollowArtisan(Long userId, Long artisanId) {
        Optional<UserFollowArtisan> existing = followRepo.findByUserIdAndArtisanId(userId, artisanId);
        if (existing.isPresent()) {
            followRepo.delete(existing.get());
            return false;
        } else {
            Artisan artisan = new Artisan();
            artisan.setId(artisanId);
            User user = new User();
            user.setId(userId);
            followRepo.save(UserFollowArtisan.builder()
                    .user(user)
                    .artisan(artisan)
                    .createdAt(LocalDateTime.now())
                    .build());
            return true;
        }
    }

    @Override
    public boolean isLessonLiked(Long userId, Long lessonId) {
        return likeRepo.existsByUserIdAndLessonId(userId, lessonId);
    }

    @Override
    public boolean isLessonSaved(Long userId, Long lessonId) {
        return saveRepo.existsByUserIdAndLessonId(userId, lessonId);
    }

    @Override
    public boolean isFollowingArtisan(Long userId, Long artisanId) {
        return followRepo.existsByUserIdAndArtisanId(userId, artisanId);
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(Long userId, Long quizId, Map<Long, Long> answers, Integer timeTakenSeconds) {
        Quiz quiz = quizService.findByIdWithQuestionsAndModule(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz không tồn tại"));
        User user = new User();
        user.setId(userId);

        int correctCount = 0;
        List<QuizQuestionResultResponse> questionResults = new ArrayList<>();

        for (QuizQuestion question : quiz.getQuestions()) {
            Long userOptionId = answers != null ? answers.get(question.getId()) : null;
            QuizOption correctOption = question.getOptions().stream()
                    .filter(QuizOption::getIsCorrect)
                    .findFirst()
                    .orElse(null);
            QuizOption userOption = userOptionId != null
                    ? question.getOptions().stream().filter(o -> o.getId().equals(userOptionId)).findFirst().orElse(null)
                    : null;

            boolean isCorrect = correctOption != null && userOption != null && correctOption.getId().equals(userOption.getId());
            if (isCorrect) correctCount++;

            questionResults.add(QuizQuestionResultResponse.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .userAnswerText(userOption != null ? userOption.getOptionText() : null)
                    .correctAnswerText(correctOption != null ? correctOption.getOptionText() : null)
                    .explanationText(question.getExplanationText())
                    .isCorrect(isCorrect)
                    .build());
        }

        int total = quiz.getQuestions().size();
        BigDecimal scorePercent = total > 0
                ? BigDecimal.valueOf(100.0 * correctCount / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String answersJson = answers != null ? answers.toString() : "{}";
        UserQuizAttempt attempt = UserQuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .correctCount(correctCount)
                .totalQuestions(total)
                .scorePercent(scorePercent)
                .timeTakenSeconds(timeTakenSeconds != null ? timeTakenSeconds : 0)
                .answersJson(answersJson)
                .submittedAt(LocalDateTime.now())
                .voucherClaimed(false)
                .build();
        attempt = attemptRepo.save(attempt);

        List<TourSummaryResponse> suggestedTours = new ArrayList<>();
        if (quiz.getModule() != null && quiz.getModule().getSuggestedTours() != null) {
            for (Tour t : quiz.getModule().getSuggestedTours()) {
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
        if (suggestedTours.isEmpty() && quiz.getModule() != null && quiz.getModule().getProvince() != null) {
            List<Tour> tours = tourService.findByProvinceId(quiz.getModule().getProvince().getId());
            for (Tour t : tours.stream().limit(3).toList()) {
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

        boolean canClaimVoucher = scorePercent.compareTo(BigDecimal.valueOf(100)) == 0
                && quiz.getAchievementVoucher() != null
                && !attempt.getVoucherClaimed();

        return QuizResultResponse.builder()
                .attemptId(attempt.getId())
                .correctCount(correctCount)
                .totalQuestions(total)
                .scorePercent(scorePercent)
                .timeTakenSeconds(timeTakenSeconds != null ? timeTakenSeconds : 0)
                .questionResults(questionResults)
                .suggestedTours(suggestedTours)
                .canClaimVoucher(canClaimVoucher)
                .build();
    }

    @Override
    @Transactional
    public void claimVoucher(Long userId, Long attemptId) {
        UserQuizAttempt attempt = attemptRepo.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lần làm quiz không tồn tại"));
        if (attempt.getVoucherClaimed()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Bạn đã nhận voucher rồi");
        }
        if (attempt.getScorePercent() == null || attempt.getScorePercent().compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể nhận voucher khi đạt 100%");
        }
        Voucher template = attempt.getQuiz().getAchievementVoucher();
        if (template == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Quiz không có voucher thưởng");
        }

        String code = "COIVIET-LEARN-" + userId + "-" + System.currentTimeMillis();
        Voucher voucher = Voucher.builder()
                .code(code)
                .discountType(template.getDiscountType())
                .discountValue(template.getDiscountValue())
                .minPurchase(template.getMinPurchase())
                .maxUsage(1)
                .currentUsage(0)
                .validFrom(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        voucher = voucherService.save(voucher);

        User user = new User();
        user.setId(userId);
        userVoucherRepo.save(UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .claimedAt(LocalDateTime.now())
                .build());

        attempt.setVoucherClaimed(true);
        attemptRepo.save(attempt);

        String discountInfo;
        if (template.getDiscountValue() == null) {
            discountInfo = "ưu đãi từ quiz Learn";
        } else if ("PERCENTAGE".equals(template.getDiscountType())) {
            discountInfo = template.getDiscountValue() + "% giảm giá";
        } else {
            discountInfo = template.getDiscountValue() + " VND giảm giá";
        }
        notificationService.createVoucherNotification(userId, code, discountInfo);
    }

    @Override
    public UserLearnStatsResponse getStats(Long userId) {
        List<UserLessonProgress> progressList = progressRepo.findByUserId(userId);

        int totalLessonsCompleted = (int) progressList.stream()
                .filter(p -> p.getCompletedAt() != null)
                .count();

        List<UserQuizAttempt> attempts = attemptRepo.findByUserIdOrderBySubmittedAtDesc(userId);
        BigDecimal averageScore = attempts.isEmpty() ? BigDecimal.ZERO
                : attempts.stream()
                .map(UserQuizAttempt::getScorePercent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(attempts.size()), 2, RoundingMode.HALF_UP);

        int streak = 0;

        Set<Long> completedModuleIds = new HashSet<>();
        for (UserLessonProgress p : progressList) {
            if (p.getCompletedAt() != null && p.getLesson() != null && p.getLesson().getModule() != null) {
                completedModuleIds.add(p.getLesson().getModule().getId());
            }
        }
        int totalCoursesCompleted = completedModuleIds.size();

        int overallProgress = completedModuleIds.isEmpty() ? 0 : 50;

        List<LearnModule> modules = moduleService.findByStatus(swd.coiviet.enums.LearnModuleStatus.PUBLISHED);
        List<LearnModuleResponse> featuredCourses = modules.stream()
                .limit(2)
                .map(this::toModuleSummary)
                .toList();

        return UserLearnStatsResponse.builder()
                .totalLessonsCompleted(totalLessonsCompleted)
                .averageScore(averageScore)
                .learningStreak(streak)
                .totalCoursesCompleted(totalCoursesCompleted)
                .overallLearningProgressPercent(overallProgress)
                .featuredCourses(featuredCourses)
                .build();
    }

    @Override
    public List<Object> getMyCourses(Long userId) {
        List<UserLessonProgress> progressList = progressRepo.findByUserId(userId);
        Set<Long> moduleIds = new HashSet<>();
        for (UserLessonProgress p : progressList) {
            if (p.getLesson() != null && p.getLesson().getModule() != null) {
                moduleIds.add(p.getLesson().getModule().getId());
            }
        }
        List<Object> result = new ArrayList<>();
        for (Long moduleId : moduleIds) {
            moduleService.findById(moduleId).ifPresent(m -> result.add(toModuleSummary(m)));
        }
        return result;
    }

    @Override
    public List<UserQuizAttempt> getMyQuizAttempts(Long userId) {
        return attemptRepo.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    private LearnModuleResponse toModuleSummary(LearnModule m) {
        List<LearnLesson> lessons = lessonService.findByModuleIdAndStatus(m.getId(), swd.coiviet.enums.PublicationStatus.PUBLISHED);
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
}
