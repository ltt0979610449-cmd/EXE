package swd.coiviet.service;

import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.model.LearnModule;

import java.util.List;
import java.util.Optional;

public interface LearnModuleService {
    LearnModule save(LearnModule m);
    Optional<LearnModule> findById(Long id);
    Optional<LearnModule> findByIdWithRelations(Long id);
    Optional<LearnModule> findBySlug(String slug);
    List<LearnModule> findByStatus(LearnModuleStatus status);
    List<LearnModule> findByCategoryIdAndStatus(Long categoryId, LearnModuleStatus status);
    List<LearnModule> findAll();
    void deleteById(Long id);
}
