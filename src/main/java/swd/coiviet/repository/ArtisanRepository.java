package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.Artisan;

import java.util.List;
import java.util.Optional;

public interface ArtisanRepository extends JpaRepository<Artisan, Long> {
    List<Artisan> findByProvinceId(Long provinceId);
    Optional<Artisan> findByUserId(Long userId);
}
