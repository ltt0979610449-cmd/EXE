package swd.coiviet.enums;

public enum PaymentStatus {
    UNPAID,           // Chưa thanh toán (MOMO/VNPay chưa pay, hoặc CASH chưa chọn)
    PENDING_CASH,     // Đã chọn CASH - chờ nhận tiền từ khách (Pay-at-Property)
    PAID, REFUNDED, FAILED
}
