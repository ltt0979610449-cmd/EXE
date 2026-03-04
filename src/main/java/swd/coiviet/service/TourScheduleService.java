package swd.coiviet.service;

import swd.coiviet.model.TourSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TourScheduleService {
    TourSchedule save(TourSchedule s);
    Optional<TourSchedule> findById(Long id);
    List<TourSchedule> findByTourIdAndDate(Long tourId, LocalDate date);
    List<TourSchedule> findByTourId(Long tourId);
    Optional<TourSchedule> findByTourIdAndTourDateAndStartTime(Long tourId, LocalDate tourDate, LocalTime startTime);
    void deleteById(Long id);
}
