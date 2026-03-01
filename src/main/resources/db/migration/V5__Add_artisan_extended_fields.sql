-- Artisan: Thêm ethnicity, dateOfBirth, images, heroSubtitle, narrativeContent, panoramaImageUrl
-- Flyway disabled - dùng Hibernate ddl-auto: update. File này để tham khảo khi enable Flyway.

ALTER TABLE artisans ADD COLUMN IF NOT EXISTS ethnicity VARCHAR(100);
ALTER TABLE artisans ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE artisans ADD COLUMN IF NOT EXISTS images TEXT;
ALTER TABLE artisans ADD COLUMN IF NOT EXISTS hero_subtitle TEXT;
ALTER TABLE artisans ADD COLUMN IF NOT EXISTS narrative_content TEXT;
ALTER TABLE artisans ADD COLUMN IF NOT EXISTS panorama_image_url TEXT;
