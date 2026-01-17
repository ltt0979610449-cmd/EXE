package swd.coiviet.service;

import swd.coiviet.model.CultureItem;

import java.util.List;
import java.util.Optional;

public interface CultureItemService {
    CultureItem save(CultureItem item);
    Optional<CultureItem> findById(Long id);
    List<CultureItem> findAllByProvinceId(Long provinceId);
    void deleteById(Long id);
}
