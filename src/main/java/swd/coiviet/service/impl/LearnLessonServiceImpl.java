package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.LearnLesson;
import swd.coiviet.repository.LearnLessonRepository;
import swd.coiviet.service.LearnLessonService;

import java.util.List;
import java.util.Optional;

@Service
public class LearnLessonServiceImpl implements LearnLessonService {
    private final LearnLessonRepository repo;

    public LearnLessonServiceImpl(LearnLessonRepository repo) {
        this.repo = repo;
    }

    @Override
    public LearnLesson save(LearnLesson l) {
        return repo.save(l);
    }

    @Override
    public Optional<LearnLesson> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<LearnLesson> findBySlug(String slug) {
        return repo.findBySlug(slug);
    }

    @Override
    public List<LearnLesson> findByModuleId(Long moduleId) {
        return repo.findByModuleIdOrderByOrderIndexAsc(moduleId);
    }

    @Override
    public List<LearnLesson> findByModuleIdAndStatus(Long moduleId, PublicationStatus status) {
        return repo.findByModuleIdAndStatusOrderByOrderIndexAsc(moduleId, status);
    }

    @Override
    @Transactional
    public void incrementViews(Long lessonId) {
        repo.findById(lessonId).ifPresent(lesson -> {
            int views = lesson.getViewsCount() != null ? lesson.getViewsCount() : 0;
            lesson.setViewsCount(views + 1);
            repo.save(lesson);
        });
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
