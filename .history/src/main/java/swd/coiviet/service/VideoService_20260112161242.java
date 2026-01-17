package swd.coiviet.service;

import swd.coiviet.model.Video;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    Video save(Video v);
    Optional<Video> findById(Long id);
    List<Video> findByProvinceId(Long provinceId);
    void deleteById(Long id);
}
