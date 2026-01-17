package swd.coiviet.service;

import swd.coiviet.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    Long countUnreadByUserId(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    void deleteById(Long id);
    
    // Helper methods for creating notifications
    Notification createBookingConfirmationNotification(Long userId, Long bookingId, String bookingCode);
    Notification createBookingCancellationNotification(Long userId, Long bookingId, String bookingCode);
    Notification createPaymentSuccessNotification(Long userId, Long bookingId, String amount);
    Notification createTourLowBookingNotification(Long userId, Long tourScheduleId, String tourTitle);
    Notification createTourCancellationNotification(Long userId, Long tourScheduleId, String tourTitle);
    Notification createTourSurchargeNotification(Long userId, Long tourScheduleId, String tourTitle, String surchargeAmount);
    Notification createAlternativeTourSuggestionNotification(Long userId, Long alternativeTourId, String tourTitle);
    Notification createVoucherNotification(Long userId, String voucherCode, String discountInfo);
}
