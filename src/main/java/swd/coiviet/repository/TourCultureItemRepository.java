package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.model.TourCultureItem;

import java.util.List;

public interface TourCultureItemRepository extends JpaRepository<TourCultureItem, Long> {
    List<TourCultureItem> findByTourIdOrderByDisplayOrderAsc(Long tourId);

    @Query("SELECT t FROM TourCultureItem t JOIN FETCH t.cultureItem c LEFT JOIN FETCH c.province WHERE t.tour.id = :tourId ORDER BY t.displayOrder ASC")
    List<TourCultureItem> findByTourIdWithCultureItemsAndProvince(@Param("tourId") Long tourId);

    void deleteByTourId(Long tourId);
}
