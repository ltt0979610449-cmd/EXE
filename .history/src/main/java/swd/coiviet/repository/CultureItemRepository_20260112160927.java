package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.CultureItem;

import java.util.List;

public interface CultureItemRepository extends JpaRepository<CultureItem, Long> {
    List<CultureItem> findByProvinceId(Long provinceId);
}
