-- Blog: Thêm cột images để lưu nhiều ảnh gallery (giống artisan)
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS images TEXT;
