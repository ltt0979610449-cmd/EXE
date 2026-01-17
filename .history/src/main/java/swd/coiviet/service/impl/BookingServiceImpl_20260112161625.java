package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Booking;
import swd.coiviet.repository.BookingRepository;
import swd.coiviet.service.BookingService;

import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repo;

    public BookingServiceImpl(BookingRepository repo) { this.repo = repo; }

    @Override
    public Booking save(Booking b) { return repo.save(b); }

    @Override
    public Optional<Booking> findById(Long id) { return repo.findById(id); }

    @Override
    public Optional<Booking> findByBookingCode(String code) { return repo.findByBookingCode(code); }

    @Override
    public List<Booking> findByUserId(Long userId) { return repo.findByUserId(userId); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
