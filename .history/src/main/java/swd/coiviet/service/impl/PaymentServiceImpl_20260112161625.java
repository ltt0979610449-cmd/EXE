package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Payment;
import swd.coiviet.repository.PaymentRepository;
import swd.coiviet.service.PaymentService;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repo;

    public PaymentServiceImpl(PaymentRepository repo) { this.repo = repo; }

    @Override
    public Payment save(Payment p) { return repo.save(p); }

    @Override
    public Optional<Payment> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Payment> findByBookingId(Long bookingId) { return repo.findByBookingId(bookingId); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
