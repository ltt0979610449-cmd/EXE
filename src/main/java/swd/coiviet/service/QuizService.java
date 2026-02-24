package swd.coiviet.service;

import swd.coiviet.model.Quiz;

import java.util.Optional;

public interface QuizService {
    Quiz save(Quiz q);
    Optional<Quiz> findById(Long id);
    Optional<Quiz> findByModuleId(Long moduleId);
    void deleteById(Long id);
}
