package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.TourCultureItem;

import java.util.List;

public interface TourCultureItemRepository extends JpaRepository<TourCultureItem, Long> {
    List<TourCultureItem> findByTourIdOrderByDisplayOrderAsc(Long tourId);
    void deleteByTourId(Long tourId);
}
