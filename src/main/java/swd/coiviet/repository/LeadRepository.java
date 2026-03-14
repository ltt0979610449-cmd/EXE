package swd.coiviet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.LeadStatus;
import swd.coiviet.model.Lead;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
    Page<Lead> findByTourId(Long tourId, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:tourId IS NULL OR l.tour.id = :tourId)")
    Page<Lead> findWithFilters(@Param("status") LeadStatus status, @Param("tourId") Long tourId, Pageable pageable);
}
