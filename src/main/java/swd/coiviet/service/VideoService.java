package swd.coiviet.service;

import swd.coiviet.model.Video;
import swd.coiviet.enums.PublicationStatus;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    Video save(Video v);
    Optional<Video> findById(Long id);
    List<Video> findAll();
    List<Video> findByProvinceId(Long provinceId);
    List<Video> findByStatus(PublicationStatus status);
    List<Video> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status);
    void deleteById(Long id);
}
