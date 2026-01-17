package swd.coiviet.service;

import swd.coiviet.model.Province;

import java.util.List;
import java.util.Optional;

public interface ProvinceService {
    Province save(Province p);
    Optional<Province> findById(Long id);
    Optional<Province> findBySlug(String slug);
    List<Province> findAll();
    void deleteById(Long id);
}
