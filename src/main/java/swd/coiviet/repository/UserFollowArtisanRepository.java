package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.model.UserFollowArtisan;

import java.util.List;
import java.util.Optional;

public interface UserFollowArtisanRepository extends JpaRepository<UserFollowArtisan, Long> {
    Optional<UserFollowArtisan> findByUserIdAndArtisanId(Long userId, Long artisanId);
    List<UserFollowArtisan> findByUserId(Long userId);
    boolean existsByUserIdAndArtisanId(Long userId, Long artisanId);

    @Query("SELECT COUNT(u) FROM UserFollowArtisan u WHERE u.artisan.id = :artisanId")
    long countByArtisanId(@Param("artisanId") Long artisanId);
}
