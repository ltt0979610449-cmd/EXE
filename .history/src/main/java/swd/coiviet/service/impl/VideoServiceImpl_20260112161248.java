package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Video;
import swd.coiviet.repository.VideoRepository;
import swd.coiviet.service.VideoService;

import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService {
    private final VideoRepository repo;

    public VideoServiceImpl(VideoRepository repo) { this.repo = repo; }

    @Override
    public Video save(Video v) { return repo.save(v); }

    @Override
    public Optional<Video> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Video> findByProvinceId(Long provinceId) { return repo.findByProvinceId(provinceId); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
