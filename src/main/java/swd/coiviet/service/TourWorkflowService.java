package swd.coiviet.service;

import swd.coiviet.dto.request.SuggestTourRequest;
import swd.coiviet.dto.response.TourSuggestionResponse;
import swd.coiviet.model.TourSchedule;

import java.util.List;

public interface TourWorkflowService {
    /**
     * Kiểm tra tính khả dụng của tour schedule
     */
    boolean checkTourAvailability(Long tourScheduleId, Integer numParticipants);
    
    /**
     * Xử lý tour có số lượng booking thấp
     * - Còn >7 ngày: Đẩy ads, telesale, khuyến mãi
     * - Còn 3-5 ngày: Gửi email cho khách, đề xuất tour dự phòng
     * - Còn <3 ngày: Quyết định hủy hoặc phụ thu
     */
    void handleLowBookingTour(TourSchedule schedule);
    
    /**
     * Gợi ý tour dựa trên vị trí (AI suggestion)
     */
    List<TourSuggestionResponse> suggestTours(SuggestTourRequest request);
    
    /**
     * Tự động kiểm tra và xử lý các tour schedules sắp tới
     */
    void processUpcomingTours();
    
    /**
     * Tạo voucher giảm giá cho tour schedule
     */
    swd.coiviet.model.Voucher createDiscountVoucherForSchedule(TourSchedule schedule, Integer discountPercent);
}
