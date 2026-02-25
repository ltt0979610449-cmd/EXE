package swd.coiviet.controller.learn;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.QuizResultResponse;
import swd.coiviet.dto.response.UserLearnStatsResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.UserQuizAttempt;
import swd.coiviet.service.UserLearnProgressService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn")
public class UserLearnController {

    private final UserLearnProgressService progressService;
    private final JwtUtil jwtUtil;

    public UserLearnController(UserLearnProgressService progressService, JwtUtil jwtUtil) {
        this.progressService = progressService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/lessons/{id}/complete")
    @Operation(summary = "Đánh dấu hoàn thành bài")
    public ResponseEntity<ApiResponse<Void>> completeLesson(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        progressService.completeLesson(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu hoàn thành"));
    }

    @PostMapping("/lessons/{id}/like")
    @Operation(summary = "Like bài")
    public ResponseEntity<ApiResponse<Boolean>> likeLesson(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean liked = progressService.toggleLessonLike(userId, id);
        return ResponseEntity.ok(ApiResponse.success(liked, liked ? "Đã thích" : "Đã bỏ thích"));
    }

    @DeleteMapping("/lessons/{id}/like")
    @Operation(summary = "Bỏ like bài")
    public ResponseEntity<ApiResponse<Boolean>> unlikeLesson(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean liked = progressService.toggleLessonLike(userId, id);
        return ResponseEntity.ok(ApiResponse.success(!liked, "Đã bỏ thích"));
    }

    @PostMapping("/lessons/{id}/save")
    @Operation(summary = "Lưu bài")
    public ResponseEntity<ApiResponse<Boolean>> saveLesson(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean saved = progressService.toggleLessonSave(userId, id);
        return ResponseEntity.ok(ApiResponse.success(saved, saved ? "Đã lưu" : "Đã bỏ lưu"));
    }

    @DeleteMapping("/lessons/{id}/save")
    @Operation(summary = "Bỏ lưu bài")
    public ResponseEntity<ApiResponse<Boolean>> unsaveLesson(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean saved = progressService.toggleLessonSave(userId, id);
        return ResponseEntity.ok(ApiResponse.success(!saved, "Đã bỏ lưu"));
    }

    @PostMapping("/artisans/{id}/follow")
    @Operation(summary = "Theo dõi nghệ nhân")
    public ResponseEntity<ApiResponse<Boolean>> followArtisan(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean following = progressService.toggleFollowArtisan(userId, id);
        return ResponseEntity.ok(ApiResponse.success(following, following ? "Đã theo dõi" : "Đã bỏ theo dõi"));
    }

    @DeleteMapping("/artisans/{id}/follow")
    @Operation(summary = "Bỏ theo dõi nghệ nhân")
    public ResponseEntity<ApiResponse<Boolean>> unfollowArtisan(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean following = progressService.toggleFollowArtisan(userId, id);
        return ResponseEntity.ok(ApiResponse.success(!following, "Đã bỏ theo dõi"));
    }

    @PostMapping("/quizzes/{id}/submit")
    @Operation(summary = "Nộp quiz")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuiz(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        @SuppressWarnings("unchecked")
        Map<String, Number> answersMap = (Map<String, Number>) body.get("answers");
        Map<Long, Long> answers = null;
        if (answersMap != null) {
            answers = new java.util.HashMap<>();
            for (Map.Entry<String, Number> e : answersMap.entrySet()) {
                answers.put(Long.parseLong(e.getKey()), e.getValue().longValue());
            }
        }
        Integer timeTaken = body.get("timeTakenSeconds") != null
                ? ((Number) body.get("timeTakenSeconds")).intValue() : 0;
        QuizResultResponse result = progressService.submitQuiz(userId, id, answers, timeTaken);
        return ResponseEntity.ok(ApiResponse.success(result, "Nộp bài thành công"));
    }

    @GetMapping("/users/me/stats")
    @Operation(summary = "Thống kê học tập")
    public ResponseEntity<ApiResponse<UserLearnStatsResponse>> getMyStats(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return ResponseEntity.ok(ApiResponse.success(progressService.getStats(userId)));
    }

    @GetMapping("/users/me/courses")
    @Operation(summary = "Khóa đang học")
    public ResponseEntity<ApiResponse<List<Object>>> getMyCourses(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return ResponseEntity.ok(ApiResponse.success(progressService.getMyCourses(userId)));
    }

    @PostMapping("/achievements/{attemptId}/claim-voucher")
    @Operation(summary = "Nhận voucher khi đạt 100%")
    public ResponseEntity<ApiResponse<Void>> claimVoucher(@PathVariable Long attemptId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        progressService.claimVoucher(userId, attemptId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã nhận voucher thành công"));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn");
            }
            Integer userId = jwtUtil.getClaims(token).get("userId", Integer.class);
            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không chứa thông tin user");
            }
            return Long.valueOf(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
        }
    }
}
