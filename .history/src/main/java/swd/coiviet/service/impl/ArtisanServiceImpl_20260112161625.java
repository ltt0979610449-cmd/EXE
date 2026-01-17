package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Artisan;
import swd.coiviet.repository.ArtisanRepository;
import swd.coiviet.service.ArtisanService;

import java.util.List;
import java.util.Optional;

@Service
public class ArtisanServiceImpl implements ArtisanService {
    private final ArtisanRepository repo;

    public ArtisanServiceImpl(ArtisanRepository repo) { this.repo = repo; }

    @Override
    public Artisan save(Artisan a) { return repo.save(a); }

    @Override
    public Optional<Artisan> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Artisan> findByProvinceId(Long provinceId) { return repo.findByProvinceId(provinceId); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
