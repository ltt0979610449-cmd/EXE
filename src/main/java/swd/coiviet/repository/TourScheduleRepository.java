package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.model.TourSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    List<TourSchedule> findByTourIdAndTourDate(Long tourId, LocalDate tourDate);
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.id = :tourId AND ts.tourDate = :tourDate AND ts.startTime = :startTime")
    Optional<TourSchedule> findByTourIdAndTourDateAndStartTime(@Param("tourId") Long tourId, @Param("tourDate") LocalDate tourDate, @Param("startTime") LocalTime startTime);
    List<TourSchedule> findByTourId(Long tourId);
    List<TourSchedule> findByTourIdAndStatus(Long tourId, TourScheduleStatus status);
    long countByStatus(TourScheduleStatus status);
    long countByTourDateGreaterThanEqual(LocalDate tourDate);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tourDate >= :fromDate AND ts.tourDate <= :toDate AND ts.status = :status")
    List<TourSchedule> findUpcomingSchedules(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, @Param("status") TourScheduleStatus status);
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.province.id = :provinceId AND ts.tourDate >= :date AND ts.status = :status ORDER BY ts.tourDate ASC")
    List<TourSchedule> findAvailableSchedulesByProvince(@Param("provinceId") Long provinceId, @Param("date") LocalDate date, @Param("status") TourScheduleStatus status);
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.id = :tourId AND ts.tourDate > :date AND ts.status = :status ORDER BY ts.tourDate ASC")
    Optional<TourSchedule> findFirstAvailableByTourId(@Param("tourId") Long tourId, @Param("date") LocalDate date, @Param("status") TourScheduleStatus status);

    @Query("SELECT DISTINCT ts.tour.id FROM TourSchedule ts WHERE ts.discountPercent IS NOT NULL AND ts.discountPercent > 0 AND ts.tourDate >= :fromDate AND ts.status = 'SCHEDULED'")
    List<Long> findDistinctTourIdsWithDiscount(@Param("fromDate") LocalDate fromDate);

    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.artisan.id = :artisanId AND ts.tourDate >= :today ORDER BY ts.tourDate ASC, ts.startTime ASC")
    List<TourSchedule> findFutureSchedulesByArtisanId(@Param("artisanId") Long artisanId, @Param("today") LocalDate today);

    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.artisan.id = :artisanId AND ts.tourDate >= :today AND ts.status = :status ORDER BY ts.tourDate ASC, ts.startTime ASC")
    List<TourSchedule> findFutureSchedulesByArtisanIdAndStatus(@Param("artisanId") Long artisanId, @Param("today") LocalDate today, @Param("status") TourScheduleStatus status);
}
