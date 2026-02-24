package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.UserFollowArtisan;

import java.util.List;
import java.util.Optional;

public interface UserFollowArtisanRepository extends JpaRepository<UserFollowArtisan, Long> {
    Optional<UserFollowArtisan> findByUserIdAndArtisanId(Long userId, Long artisanId);
    List<UserFollowArtisan> findByUserId(Long userId);
    boolean existsByUserIdAndArtisanId(Long userId, Long artisanId);
}
