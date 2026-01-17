package swd.coiviet.service;

import swd.coiviet.model.Tour;

import java.util.List;
import java.util.Optional;

public interface TourService {
    Tour save(Tour t);
    Optional<Tour> findById(Long id);
    List<Tour> findByProvinceId(Long provinceId);
    List<Tour> findAll();
    void deleteById(Long id);
}
