package swd.coiviet.service;

import swd.coiviet.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking save(Booking b);
    Optional<Booking> findById(Long id);
    Optional<Booking> findByBookingCode(String code);
    List<Booking> findByUserId(Long userId);
    void deleteById(Long id);
}
