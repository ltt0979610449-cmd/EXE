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

    /** JSON array các block: [{"title":"...","content":"...","imageUrl":"..."}] (giống artisan) */
    @Column(name = "narrative_content", columnDefinition = "text")
    private String narrativeContent;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(columnDefinition = "text")
    private String featuredImageUrl;

    /** Mô tả ngắn cho hero section (giống artisan) */
    @Column(name = "hero_subtitle", columnDefinition = "text")
    private String heroSubtitle;

    /** Ảnh panorama full-width (giống artisan) */
    @Column(name = "panorama_image_url", columnDefinition = "text")
    private String panoramaImageUrl;

    /** URL ảnh gallery, phân cách bằng dấu phẩy (giống artisan) */
    @Column(columnDefinition = "text")
    private String images;

    @Enumerated(EnumType.STRING)
    private PublicationStatus status;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;
}
