package swd.coiviet.service;

import swd.coiviet.dto.request.CancelBookingRequest;
import swd.coiviet.dto.request.CreateBookingRequest;
import swd.coiviet.dto.response.BookingResponse;
import swd.coiviet.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking save(Booking b);
    Optional<Booking> findById(Long id);
    Optional<Booking> findByBookingCode(String code);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByTourScheduleId(Long tourScheduleId);
    void deleteById(Long id);
    
    // New methods for workflow
    BookingResponse createBooking(Long userId, CreateBookingRequest request);
    BookingResponse cancelBooking(Long userId, Long bookingId, CancelBookingRequest request);
    java.math.BigDecimal calculateCancellationFee(Booking booking);
}
