package swd.coiviet.model;

import jakarta.persistence.*;
import lombok.*;
import swd.coiviet.enums.PublicationStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(columnDefinition = "text")
    private String featuredImageUrl;

    @Enumerated(EnumType.STRING)
    private PublicationStatus status;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;
}
