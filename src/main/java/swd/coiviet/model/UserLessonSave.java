package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_lesson_saves", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLessonSave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private LearnLesson lesson;

    private LocalDateTime createdAt;
}
