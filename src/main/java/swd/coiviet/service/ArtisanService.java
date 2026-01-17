package swd.coiviet.service;

import swd.coiviet.model.Artisan;

import java.util.List;
import java.util.Optional;

public interface ArtisanService {
    Artisan save(Artisan a);
    Optional<Artisan> findById(Long id);
    List<Artisan> findAll();
    List<Artisan> findByProvinceId(Long provinceId);
    Optional<Artisan> findByUserId(Long userId);
    void deleteById(Long id);
}
