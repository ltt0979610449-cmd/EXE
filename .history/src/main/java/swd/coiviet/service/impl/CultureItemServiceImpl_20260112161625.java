package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.CultureItem;
import swd.coiviet.repository.CultureItemRepository;
import swd.coiviet.service.CultureItemService;

import java.util.List;
import java.util.Optional;

@Service
public class CultureItemServiceImpl implements CultureItemService {
    private final CultureItemRepository repo;

    public CultureItemServiceImpl(CultureItemRepository repo) { this.repo = repo; }

    @Override
    public CultureItem save(CultureItem item) { return repo.save(item); }

    @Override
    public Optional<CultureItem> findById(Long id) { return repo.findById(id); }

    @Override
    public List<CultureItem> findAllByProvinceId(Long provinceId) { return repo.findByProvinceId(provinceId); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
