package swd.coiviet.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendBookingConfirmation(String to, String bookingCode, String tourTitle, String tourDate);
    void sendBookingCancellation(String to, String bookingCode, String reason, java.math.BigDecimal refundAmount);
    void sendTourLowBookingAlert(String to, String tourTitle, String tourDate, String voucherCode, Integer discountPercent);
    void sendTourCancellationNotice(String to, String tourTitle, String tourDate, String reason);
    void sendTourSurchargeNotice(String to, String tourTitle, String tourDate, java.math.BigDecimal surchargeAmount);
    void sendAlternativeTourSuggestion(String to, String originalTourTitle, java.util.List<String> alternativeTours);
}
