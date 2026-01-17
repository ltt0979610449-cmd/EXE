package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.model.PasswordResetToken;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.email = :email AND prt.used = false ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findByUserEmailAndUsedFalse(@Param("email") String email);
    List<PasswordResetToken> findByUserIdAndUsedFalse(Long userId);
}
