package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Quiz;
import swd.coiviet.repository.QuizRepository;
import swd.coiviet.service.QuizService;

import java.util.Optional;

@Service
public class QuizServiceImpl implements QuizService {
    private final QuizRepository repo;

    public QuizServiceImpl(QuizRepository repo) {
        this.repo = repo;
    }

    @Override
    public Quiz save(Quiz q) {
        return repo.save(q);
    }

    @Override
    public Optional<Quiz> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<Quiz> findByModuleId(Long moduleId) {
        return repo.findByModuleId(moduleId);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
