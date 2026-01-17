package swd.coiviet.service;

import swd.coiviet.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment save(Payment p);
    Optional<Payment> findById(Long id);
    List<Payment> findByBookingId(Long bookingId);
    void deleteById(Long id);
}
