package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Notification;
import swd.coiviet.repository.NotificationRepository;
import swd.coiviet.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repo;

    public NotificationServiceImpl(NotificationRepository repo) {
        this.repo = repo;
    }

    @Override
    public Notification save(Notification notification) {
        return repo.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> findByUserIdAndIsReadFalse(Long userId) {
        return repo.findByUserIdAndIsReadFalse(userId);
    }

    @Override
    public Long countUnreadByUserId(Long userId) {
        return repo.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        repo.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            repo.save(notification);
        });
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unread = repo.findByUserIdAndIsReadFalse(userId);
        unread.forEach(notification -> notification.setIsRead(true));
        repo.saveAll(unread);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Notification createBookingConfirmationNotification(Long userId, Long bookingId, String bookingCode) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("BOOKING_CONFIRMED")
                .title("Xác nhận đặt tour")
                .message(String.format("Đặt tour của bạn với mã %s đã được xác nhận. Vui lòng thanh toán để hoàn tất.", bookingCode))
                .relatedId(bookingId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }

    @Override
    public Notification createBookingCancellationNotification(Long userId, Long bookingId, String bookingCode) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("BOOKING_CANCELLED")
                .title("Hủy đặt tour")
                .message(String.format("Đặt tour của bạn với mã %s đã bị hủy.", bookingCode))
                .relatedId(bookingId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }

    @Override
    public Notification createPaymentSuccessNotification(Long userId, Long bookingId, String amount) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("PAYMENT_SUCCESS")
                .title("Thanh toán thành công")
                .message(String.format("Bạn đã thanh toán thành công số tiền %s cho đặt tour.", amount))
                .relatedId(bookingId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }

    @Override
    public Notification createTourLowBookingNotification(Long userId, Long tourScheduleId, String tourTitle) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("TOUR_LOW_BOOKING")
                .title("Tour sắp khởi hành")
                .message(String.format("Tour '%s' sắp khởi hành nhưng chưa đủ số lượng. Chúng tôi đang có chương trình giảm giá đặc biệt!", tourTitle))
                .relatedId(tourScheduleId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }

    @Override
    public Notification createTourCancellationNotification(Long userId, Long tourScheduleId, String tourTitle) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("TOUR_CANCELLED")
                .title("Tour bị hủy")
                .message(String.format("Rất tiếc, tour '%s' đã bị hủy do không đủ số lượng. Chúng tôi sẽ liên hệ với bạn để đề xuất tour thay thế.", tourTitle))
                .relatedId(tourScheduleId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }

    @Override
    public Notification createTourSurchargeNotification(Long userId, Long tourScheduleId, String tourTitle, String surchargeAmount) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("TOUR_SURCHARGE")
                .title("Phụ thu tour")
                .message(String.format("Tour '%s' có phụ thu %s do số lượng đăng ký thấp. Vui lòng xác nhận để tiếp tục.", tourTitle, surchargeAmount))
                .relatedId(tourScheduleId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }

    @Override
    public Notification createAlternativeTourSuggestionNotification(Long userId, Long alternativeTourId, String tourTitle) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("ALTERNATIVE_TOUR")
                .title("Đề xuất tour thay thế")
                .message(String.format("Chúng tôi đề xuất tour '%s' như một lựa chọn thay thế phù hợp với bạn.", tourTitle))
                .relatedId(alternativeTourId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }

    @Override
    public Notification createVoucherNotification(Long userId, String voucherCode, String discountInfo) {
        Notification notification = Notification.builder()
                .user(swd.coiviet.model.User.builder().id(userId).build())
                .type("VOUCHER")
                .title("Nhận voucher giảm giá")
                .message(String.format("Bạn đã nhận được voucher %s với %s. Sử dụng ngay để được giảm giá!", voucherCode, discountInfo))
                .relatedId(null)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return save(notification);
    }
}
