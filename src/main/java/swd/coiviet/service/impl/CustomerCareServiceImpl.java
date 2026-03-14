package swd.coiviet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.model.Booking;
import swd.coiviet.model.Tour;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.repository.BookingRepository;
import swd.coiviet.service.BookingService;
import swd.coiviet.service.CustomerCareService;
import swd.coiviet.service.EmailService;
import swd.coiviet.service.TourScheduleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerCareServiceImpl implements CustomerCareService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerCareServiceImpl.class);
    private static final int PRE_DEPARTURE_DAYS = 3;
    private final BookingRepository bookingRepo;
    private final BookingService bookingService;
    private final TourScheduleService tourScheduleService;
    private final EmailService emailService;

    @Value("${app.feedback-base-url:https://exe-project-two.vercel.app}")
    private String feedbackBaseUrl;

    public CustomerCareServiceImpl(BookingRepository bookingRepo, BookingService bookingService,
                                   TourScheduleService tourScheduleService, EmailService emailService) {
        this.bookingRepo = bookingRepo;
        this.bookingService = bookingService;
        this.tourScheduleService = tourScheduleService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void sendPreDepartureReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(PRE_DEPARTURE_DAYS);
        List<Booking> bookings = bookingRepo.findForPreDepartureEmail(targetDate, BookingStatus.CONFIRMED);

        logger.info("Gửi email nhắc lịch cho {} booking (tour khởi hành ngày {})", bookings.size(), targetDate);

        for (Booking b : bookings) {
            try {
                String email = b.getContactEmail() != null ? b.getContactEmail() : b.getUser().getEmail();
                if (email == null || email.isBlank()) continue;

                Tour tour = b.getTour();
                TourSchedule schedule = b.getTourSchedule();
                String preparationTips = tour.getPreparationTips();
                String tourDateStr = schedule.getTourDate() != null ? schedule.getTourDate().toString() : "";

                emailService.sendPreDepartureReminder(
                        email,
                        b.getBookingCode(),
                        tour.getTitle(),
                        tourDateStr,
                        preparationTips
                );

                b.setPreDepartureEmailSentAt(LocalDateTime.now());
                bookingService.save(b);
                logger.info("Đã gửi nhắc lịch cho booking {}", b.getBookingCode());
            } catch (Exception e) {
                logger.error("Lỗi gửi email nhắc lịch cho booking {}: {}", b.getBookingCode(), e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void processPostTourAndSendFeedbackRequests() {
        // Tour đã kết thúc (tourDate < hôm nay)
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepo.findForPostTourFeedback(today, BookingStatus.CONFIRMED);

        logger.info("Xử lý post-tour và gửi feedback cho {} booking (tour kết thúc trước {})", bookings.size(), today);

        for (Booking b : bookings) {
            try {
                TourSchedule schedule = b.getTourSchedule();
                Tour tour = b.getTour();

                // Cập nhật tour schedule sang COMPLETED nếu cần
                if (schedule != null && schedule.getStatus() != TourScheduleStatus.CANCELLED) {
                    schedule.setStatus(TourScheduleStatus.COMPLETED);
                    tourScheduleService.save(schedule);
                }

                // Cập nhật booking sang COMPLETED
                b.setStatus(BookingStatus.COMPLETED);
                b.setUpdatedAt(LocalDateTime.now());

                // Gửi email xin feedback
                String email = b.getContactEmail() != null ? b.getContactEmail() : (b.getUser() != null ? b.getUser().getEmail() : null);
                if (email != null && !email.isBlank()) {
                    String feedbackLink = feedbackBaseUrl + "/bookings/" + b.getId() + "/review";
                    emailService.sendPostTourFeedbackRequest(
                            email,
                            b.getBookingCode(),
                            tour.getTitle(),
                            feedbackLink
                    );
                    b.setPostTourFeedbackEmailSentAt(LocalDateTime.now());
                }

                bookingService.save(b);
                logger.info("Đã xử lý post-tour và gửi feedback cho booking {}", b.getBookingCode());
            } catch (Exception e) {
                logger.error("Lỗi xử lý post-tour cho booking {}: {}", b.getBookingCode(), e.getMessage(), e);
            }
        }
    }
}