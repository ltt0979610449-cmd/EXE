package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.enums.Status;
import swd.coiviet.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(Status status);

}
