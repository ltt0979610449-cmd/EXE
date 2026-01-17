package swd.coiviet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // General errors
    INVALID_REQUEST(400, "Invalid request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway timeout"),
    
    // Authentication errors
    INVALID_CREDENTIALS(401, "Invalid username or password"),
    ACCOUNT_DISABLED(403, "Account is disabled"),
    ACCOUNT_LOCKED(403, "Account is locked"),
    TOKEN_EXPIRED(401, "Token has expired"),
    TOKEN_INVALID(401, "Token is invalid"),
    
    // Booking errors
    TOUR_NOT_AVAILABLE(400, "Tour is not available"),
    INSUFFICIENT_SLOTS(400, "Insufficient slots available"),
    BOOKING_ALREADY_CANCELLED(400, "Booking is already cancelled"),
    BOOKING_ALREADY_COMPLETED(400, "Booking is already completed"),
    CANNOT_CANCEL_BOOKING(400, "Cannot cancel booking at this time"),
    VOUCHER_INVALID(400, "Voucher code is invalid or expired"),
    VOUCHER_ALREADY_USED(400, "Voucher has already been used"),
    PAYMENT_REQUIRED(402, "Payment is required"),
    PAYMENT_FAILED(402, "Payment failed"),
    
    // Tour errors
    TOUR_SCHEDULE_NOT_FOUND(404, "Tour schedule not found"),
    TOUR_SCHEDULE_FULL(400, "Tour schedule is full"),
    TOUR_SCHEDULE_CANCELLED(400, "Tour schedule has been cancelled"),
    TOUR_DATE_PASSED(400, "Tour date has already passed"),
    
    // User errors
    USER_NOT_FOUND(404, "User not found"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),
    USERNAME_ALREADY_EXISTS(400, "Username already exists"),
    
    // Validation errors
    INVALID_EMAIL(400, "Invalid email format"),
    INVALID_PHONE(400, "Invalid phone number"),
    PASSWORD_TOO_SHORT(400, "Password is too short"),
    REQUIRED_FIELD_MISSING(400, "Required field is missing");

    private final int code;
    private final String message;

    public HttpStatus getHttpStatus() {
        return switch (this) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case GATEWAY_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
