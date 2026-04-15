package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.repository.TourScheduleRepository;
import swd.coiviet.service.TourScheduleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class TourScheduleServiceImpl implements TourScheduleService {
    private final TourScheduleRepository repo;

    public TourScheduleServiceImpl(TourScheduleRepository repo) { this.repo = repo; }

    @Override
    public TourSchedule save(TourSchedule s) { return repo.save(s); }

    @Override
    public Optional<TourSchedule> findById(Long id) { return repo.findById(id); }

    @Override
    public List<TourSchedule> findByTourIdAndDate(Long tourId, LocalDate date) {
        return repo.findByTourIdAndTourDate(tourId, date);
    }

    @Override
    public List<TourSchedule> findByTourId(Long tourId) { return repo.findByTourId(tourId); }

    @Override
    public Optional<TourSchedule> findByTourIdAndTourDateAndStartTime(Long tourId, LocalDate tourDate, LocalTime startTime) {
        return repo.findByTourIdAndTourDateAndStartTime(tourId, tourDate, startTime);
    }

    @Override
    public List<TourSchedule> findFutureSchedulesByArtisanId(Long artisanId) {
        return repo.findFutureSchedulesByArtisanId(artisanId, LocalDate.now());
    }

    @Override
    public List<TourSchedule> findFutureSchedulesByArtisanIdAndStatus(Long artisanId, TourScheduleStatus status) {
        return repo.findFutureSchedulesByArtisanIdAndStatus(artisanId, LocalDate.now(), status);
    }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
