package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.model.Booking;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByTourScheduleId(Long tourScheduleId);
    List<Booking> findByTourScheduleIdAndStatus(Long tourScheduleId, BookingStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(BookingStatus status);
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tourSchedule.id = :scheduleId AND b.status != :status")
    Integer countByTourScheduleIdExcludingStatus(@Param("scheduleId") Long scheduleId, @Param("status") BookingStatus status);
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tour.artisan.id = :artisanId")
    long countByArtisanId(@Param("artisanId") Long artisanId);
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tour.artisan.id = :artisanId AND b.createdAt BETWEEN :start AND :end")
    long countByArtisanIdAndCreatedAtBetween(@Param("artisanId") Long artisanId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("SELECT b FROM Booking b WHERE b.tour.artisan.id = :artisanId")
    List<Booking> findByArtisanId(@Param("artisanId") Long artisanId);
}
