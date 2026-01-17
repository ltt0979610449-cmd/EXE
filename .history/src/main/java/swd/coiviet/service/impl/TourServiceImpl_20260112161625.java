package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Tour;
import swd.coiviet.repository.TourRepository;
import swd.coiviet.service.TourService;

import java.util.List;
import java.util.Optional;

@Service
public class TourServiceImpl implements TourService {
    private final TourRepository repo;

    public TourServiceImpl(TourRepository repo) { this.repo = repo; }

    @Override
    public Tour save(Tour t) { return repo.save(t); }

    @Override
    public Optional<Tour> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Tour> findByProvinceId(Long provinceId) { return repo.findByProvinceId(provinceId); }

    @Override
    public List<Tour> findAll() { return repo.findAll(); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
