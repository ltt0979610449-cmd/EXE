package swd.coiviet.service;

/**
 * Dịch vụ chăm sóc khách hàng: gửi email nhắc lịch trước ngày đi, xin feedback sau tour.
 */
public interface CustomerCareService {
    /**
     * Gửi email nhắc lịch cho các booking có tour khởi hành sau 3 ngày.
     */
    void sendPreDepartureReminders();

    /**
     * Chuyển booking sang COMPLETED và gửi email xin feedback cho các tour đã kết thúc.
     */
    void processPostTourAndSendFeedbackRequests();
}
