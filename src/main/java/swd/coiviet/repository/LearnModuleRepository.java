package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.model.LearnModule;

import java.util.List;
import java.util.Optional;

public interface LearnModuleRepository extends JpaRepository<LearnModule, Long> {

    List<LearnModule> findByStatusOrderByOrderIndexAsc(LearnModuleStatus status);
    List<LearnModule> findByProvinceIdAndStatusOrderByOrderIndexAsc(Long provinceId, LearnModuleStatus status);
    List<LearnModule> findByCategoryIdAndStatusOrderByOrderIndexAsc(Long categoryId, LearnModuleStatus status);
    Optional<LearnModule> findBySlug(String slug);

    @Query("SELECT DISTINCT m FROM LearnModule m LEFT JOIN FETCH m.quiz q LEFT JOIN FETCH q.questions WHERE m.id = :id")
    Optional<LearnModule> findWithQuizById(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM LearnModule m LEFT JOIN FETCH m.suggestedTours t LEFT JOIN FETCH t.province WHERE m.id = :id")
    Optional<LearnModule> findWithToursById(@Param("id") Long id);
}
