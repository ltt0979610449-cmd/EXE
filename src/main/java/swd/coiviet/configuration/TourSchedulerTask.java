package swd.coiviet.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import swd.coiviet.service.CustomerCareService;
import swd.coiviet.service.TourWorkflowService;

@Component
public class TourSchedulerTask {
    private static final Logger logger = LoggerFactory.getLogger(TourSchedulerTask.class);

    private final TourWorkflowService tourWorkflowService;
    private final CustomerCareService customerCareService;

    public TourSchedulerTask(TourWorkflowService tourWorkflowService, CustomerCareService customerCareService) {
        this.tourWorkflowService = tourWorkflowService;
        this.customerCareService = customerCareService;
    }

    /**
     * Chạy mỗi ngày lúc 2:00 AM để xử lý các tour sắp tới
     * Kiểm tra và xử lý các tour có số lượng booking thấp
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2:00 AM mỗi ngày
    public void processUpcomingTours() {
        logger.info("Bắt đầu scheduled task: Xử lý các tour sắp tới");
        try {
            tourWorkflowService.processUpcomingTours();
            logger.info("Hoàn thành scheduled task: Xử lý các tour sắp tới");
        } catch (Exception e) {
            logger.error("Lỗi khi chạy scheduled task xử lý tour: {}", e.getMessage(), e);
        }
    }

    /**
     * Chạy mỗi 6 giờ để kiểm tra các tour cần xử lý ngay
     */
    @Scheduled(cron = "0 0 */6 * * ?") // Mỗi 6 giờ
    public void checkUrgentTours() {
        logger.info("Bắt đầu scheduled task: Kiểm tra các tour cần xử lý ngay");
        try {
            tourWorkflowService.processUpcomingTours();
            logger.info("Hoàn thành scheduled task: Kiểm tra các tour cần xử lý ngay");
        } catch (Exception e) {
            logger.error("Lỗi khi chạy scheduled task kiểm tra tour: {}", e.getMessage(), e);
        }
    }

    /**
     * Chạy mỗi ngày lúc 8:00 AM - Gửi email nhắc lịch trước 3 ngày đi
     */
    @Scheduled(cron = "0 0 8 * * ?") // 8:00 AM mỗi ngày
    public void sendPreDepartureReminders() {
        logger.info("Bắt đầu scheduled task: Gửi email nhắc lịch trước ngày đi");
        try {
            customerCareService.sendPreDepartureReminders();
            logger.info("Hoàn thành scheduled task: Gửi email nhắc lịch");
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email nhắc lịch: {}", e.getMessage(), e);
        }
    }

    /**
     * Chạy mỗi ngày lúc 9:00 AM - Xử lý post-tour: COMPLETED + gửi email xin feedback
     */
    @Scheduled(cron = "0 0 9 * * ?") // 9:00 AM mỗi ngày
    public void processPostTourAndSendFeedback() {
        logger.info("Bắt đầu scheduled task: Xử lý post-tour và gửi feedback");
        try {
            customerCareService.processPostTourAndSendFeedbackRequests();
            logger.info("Hoàn thành scheduled task: Post-tour và feedback");
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý post-tour: {}", e.getMessage(), e);
        }
    }
}
