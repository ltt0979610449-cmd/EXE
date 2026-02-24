package swd.coiviet.service;

import swd.coiviet.dto.response.QuizResultResponse;
import swd.coiviet.dto.response.UserLearnStatsResponse;
import swd.coiviet.model.UserQuizAttempt;

import java.util.List;
import java.util.Map;

public interface UserLearnProgressService {
    void completeLesson(Long userId, Long lessonId);
    boolean toggleLessonLike(Long userId, Long lessonId);
    boolean toggleLessonSave(Long userId, Long lessonId);
    boolean toggleFollowArtisan(Long userId, Long artisanId);
    boolean isLessonLiked(Long userId, Long lessonId);
    boolean isLessonSaved(Long userId, Long lessonId);
    boolean isFollowingArtisan(Long userId, Long artisanId);
    QuizResultResponse submitQuiz(Long userId, Long quizId, Map<Long, Long> answers, Integer timeTakenSeconds);
    void claimVoucher(Long userId, Long attemptId);
    UserLearnStatsResponse getStats(Long userId);
    List<Object> getMyCourses(Long userId);
    List<UserQuizAttempt> getMyQuizAttempts(Long userId);
}
