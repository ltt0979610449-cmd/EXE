package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Province;
import swd.coiviet.repository.ProvinceRepository;
import swd.coiviet.service.ProvinceService;

import java.util.List;
import java.util.Optional;

@Service
public class ProvinceServiceImpl implements ProvinceService {
    private final ProvinceRepository repo;

    public ProvinceServiceImpl(ProvinceRepository repo) { this.repo = repo; }

    @Override
    public Province save(Province p) { return repo.save(p); }

    @Override
    public Optional<Province> findById(Long id) { return repo.findById(id); }

    @Override
    public Optional<Province> findBySlug(String slug) { return repo.findBySlug(slug); }

    @Override
    public List<Province> findAll() { return repo.findAll(); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
