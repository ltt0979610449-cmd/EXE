package swd.coiviet.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import swd.coiviet.service.TourWorkflowService;

@Component
public class TourSchedulerTask {
    private static final Logger logger = LoggerFactory.getLogger(TourSchedulerTask.class);
    
    private final TourWorkflowService tourWorkflowService;

    public TourSchedulerTask(TourWorkflowService tourWorkflowService) {
        this.tourWorkflowService = tourWorkflowService;
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
}
