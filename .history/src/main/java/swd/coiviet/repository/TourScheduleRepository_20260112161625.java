package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.TourSchedule;

import java.time.LocalDate;
import java.util.List;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    List<TourSchedule> findByTourIdAndTourDate(Long tourId, LocalDate tourDate);
}
