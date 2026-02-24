package swd.coiviet.service;

import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.LearnLesson;

import java.util.List;
import java.util.Optional;

public interface LearnLessonService {
    LearnLesson save(LearnLesson l);
    Optional<LearnLesson> findById(Long id);
    Optional<LearnLesson> findBySlug(String slug);
    List<LearnLesson> findByModuleId(Long moduleId);
    List<LearnLesson> findByModuleIdAndStatus(Long moduleId, PublicationStatus status);
    void incrementViews(Long lessonId);
    void deleteById(Long id);
}
