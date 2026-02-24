package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.model.LearnModule;

import java.util.List;
import java.util.Optional;

public interface LearnModuleRepository extends JpaRepository<LearnModule, Long> {

    List<LearnModule> findByStatusOrderByOrderIndexAsc(LearnModuleStatus status);
    List<LearnModule> findByCategoryIdAndStatusOrderByOrderIndexAsc(Long categoryId, LearnModuleStatus status);
    Optional<LearnModule> findBySlug(String slug);
}
