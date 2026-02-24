package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learn_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String slug;

    private Integer orderIndex;

    private Boolean isActive;
}
