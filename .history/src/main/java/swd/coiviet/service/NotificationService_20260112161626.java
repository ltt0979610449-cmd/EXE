package swd.coiviet.service;

import swd.coiviet.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    Notification save(Notification n);
    Optional<Notification> findById(Long id);
    List<Notification> findByUserId(Long userId);
    void deleteById(Long id);
}
