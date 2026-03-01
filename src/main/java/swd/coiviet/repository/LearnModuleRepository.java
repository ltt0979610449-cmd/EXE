package swd.coiviet.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.model.LearnModule;

import java.util.List;
import java.util.Optional;

public interface LearnModuleRepository extends JpaRepository<LearnModule, Long> {

    List<LearnModule> findByStatusOrderByOrderIndexAsc(LearnModuleStatus status);
    List<LearnModule> findByCategoryIdAndStatusOrderByOrderIndexAsc(Long categoryId, LearnModuleStatus status);
    Optional<LearnModule> findBySlug(String slug);

    @EntityGraph(attributePaths = {"quiz", "quiz.questions", "suggestedTours", "suggestedTours.province"})
    @Query("SELECT m FROM LearnModule m WHERE m.id = :id")
    Optional<LearnModule> findWithQuizAndToursById(@Param("id") Long id);
}
