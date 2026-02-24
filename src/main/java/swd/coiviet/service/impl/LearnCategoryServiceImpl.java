package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.LearnCategory;
import swd.coiviet.repository.LearnCategoryRepository;
import swd.coiviet.service.LearnCategoryService;

import java.util.List;
import java.util.Optional;

@Service
public class LearnCategoryServiceImpl implements LearnCategoryService {
    private final LearnCategoryRepository repo;

    public LearnCategoryServiceImpl(LearnCategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public LearnCategory save(LearnCategory c) {
        return repo.save(c);
    }

    @Override
    public Optional<LearnCategory> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<LearnCategory> findBySlug(String slug) {
        return repo.findBySlug(slug);
    }

    @Override
    public List<LearnCategory> findAllActive() {
        return repo.findByIsActiveTrueOrderByOrderIndexAsc();
    }

    @Override
    public List<LearnCategory> findAll() {
        return repo.findAll();
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
