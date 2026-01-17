package swd.coiviet.service;

import swd.coiviet.model.CultureItem;
import swd.coiviet.enums.CultureCategory;
import swd.coiviet.enums.PublicationStatus;

import java.util.List;
import java.util.Optional;

public interface CultureItemService {
    CultureItem save(CultureItem item);
    Optional<CultureItem> findById(Long id);
    List<CultureItem> findAll();
    List<CultureItem> findAllByProvinceId(Long provinceId);
    List<CultureItem> findByCategory(CultureCategory category);
    List<CultureItem> findByProvinceIdAndCategory(Long provinceId, CultureCategory category);
    List<CultureItem> findByStatus(PublicationStatus status);
    List<CultureItem> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status);
    void deleteById(Long id);
}
