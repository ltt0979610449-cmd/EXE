-- Blog: Thêm heroSubtitle, panoramaImageUrl, narrativeContent (format giống artisan)
-- Flyway disabled - dùng Hibernate ddl-auto: update. File này để tham khảo khi enable Flyway.
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS hero_subtitle TEXT;
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS panorama_image_url TEXT;
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS narrative_content TEXT;

-- Nếu có cột blocks_json cũ, migrate dữ liệu rồi xóa (chạy thủ công nếu cần):
-- UPDATE blog_posts SET narrative_content = blocks_json WHERE blocks_json IS NOT NULL AND (narrative_content IS NULL OR narrative_content = '');
-- ALTER TABLE blog_posts DROP COLUMN IF EXISTS blocks_json;
