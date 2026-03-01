-- Tour: Thêm bestSeason, transportation, culturalTips
-- Flyway disabled - dùng Hibernate ddl-auto: update. File này để tham khảo khi enable Flyway.

ALTER TABLE tours ADD COLUMN IF NOT EXISTS best_season TEXT;
ALTER TABLE tours ADD COLUMN IF NOT EXISTS transportation TEXT;
ALTER TABLE tours ADD COLUMN IF NOT EXISTS cultural_tips TEXT;
