package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Notification;
import swd.coiviet.repository.NotificationRepository;
import swd.coiviet.service.NotificationService;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repo;

    public NotificationServiceImpl(NotificationRepository repo) { this.repo = repo; }

    @Override
    public Notification save(Notification n) { return repo.save(n); }

    @Override
    public Optional<Notification> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Notification> findByUserId(Long userId) { return repo.findByUserId(userId); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
