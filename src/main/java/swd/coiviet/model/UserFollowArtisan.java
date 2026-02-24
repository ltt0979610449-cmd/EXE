package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_follow_artisans", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "artisan_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollowArtisan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    private LocalDateTime createdAt;
}
