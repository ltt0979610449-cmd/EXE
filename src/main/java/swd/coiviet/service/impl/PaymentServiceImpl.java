package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Payment save(Payment p) { return repo.save(p); }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) { return repo.findById(id); }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionId(String transactionId) { 
        return repo.findByTransactionId(transactionId); 
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByBookingId(Long bookingId) { return repo.findByBookingId(bookingId); }

    @Override
    @Transactional
    public void deleteById(Long id) { repo.deleteById(id); }
}
