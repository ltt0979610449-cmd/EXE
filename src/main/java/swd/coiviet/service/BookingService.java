package swd.coiviet.service;

import swd.coiviet.dto.request.CancelBookingRequest;
import swd.coiviet.dto.request.CreateBookingRequest;
import swd.coiviet.dto.response.BookingResponse;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.model.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking save(Booking b);
    Optional<Booking> findById(Long id);
    Optional<Booking> findByBookingCode(String code);
    List<Booking> findAll();
    List<Booking> findByUserId(Long userId);
    List<Booking> findByTourScheduleId(Long tourScheduleId);
    void deleteById(Long id);

    List<Booking> findByArtisanIdWithFilters(Long artisanId, BookingStatus status, LocalDate from, LocalDate to);
    BookingResponse confirmBookingByArtisan(Long artisanId, Long bookingId);
    BookingResponse cancelBookingByArtisan(Long artisanId, Long bookingId, CancelBookingRequest request);

    // New methods for workflow
    BookingResponse createBooking(Long userId, CreateBookingRequest request);
    BookingResponse cancelBooking(Long userId, Long bookingId, CancelBookingRequest request);
    java.math.BigDecimal calculateCancellationFee(Booking booking);
    BookingResponse toResponse(Booking booking);

    /** Tăng totalBookings của tour khi thanh toán thành công */
    void incrementTourTotalBookings(Booking booking);

    /** Giảm totalBookings của tour khi hủy booking đã thanh toán */
    void decrementTourTotalBookings(Booking booking);

    /** Cập nhật trạng thái booking thủ công (chỉ ADMIN/STAFF). Cho phép: PENDING→CONFIRMED, CONFIRMED→COMPLETED */
    BookingResponse updateBookingStatus(Long bookingId, BookingStatus status);
}
