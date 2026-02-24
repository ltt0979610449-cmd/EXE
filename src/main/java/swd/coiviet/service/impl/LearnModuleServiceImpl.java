package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.model.LearnModule;
import swd.coiviet.repository.LearnModuleRepository;
import swd.coiviet.service.LearnModuleService;

import java.util.List;
import java.util.Optional;

@Service
public class LearnModuleServiceImpl implements LearnModuleService {
    private final LearnModuleRepository repo;

    public LearnModuleServiceImpl(LearnModuleRepository repo) {
        this.repo = repo;
    }

    @Override
    public LearnModule save(LearnModule m) {
        return repo.save(m);
    }

    @Override
    public Optional<LearnModule> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<LearnModule> findBySlug(String slug) {
        return repo.findBySlug(slug);
    }

    @Override
    public List<LearnModule> findByStatus(LearnModuleStatus status) {
        return repo.findByStatusOrderByOrderIndexAsc(status);
    }

    @Override
    public List<LearnModule> findByCategoryIdAndStatus(Long categoryId, LearnModuleStatus status) {
        return repo.findByCategoryIdAndStatusOrderByOrderIndexAsc(categoryId, status);
    }

    @Override
    public List<LearnModule> findAll() {
        return repo.findAll();
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
