-- Province: Thêm bestSeason, transportation, culturalTips
-- Flyway disabled - dùng Hibernate ddl-auto: update. File này để tham khảo khi enable Flyway.

ALTER TABLE provinces ADD COLUMN IF NOT EXISTS best_season TEXT;
ALTER TABLE provinces ADD COLUMN IF NOT EXISTS transportation TEXT;
ALTER TABLE provinces ADD COLUMN IF NOT EXISTS cultural_tips TEXT;

-- Bảng tour_culture_items (many-to-many Tour <-> CultureItem)
CREATE TABLE IF NOT EXISTS tour_culture_items (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    culture_item_id BIGINT NOT NULL REFERENCES culture_items(id) ON DELETE CASCADE,
    display_order INTEGER,
    highlight_type VARCHAR(50),
    UNIQUE(tour_id, culture_item_id)
);

CREATE INDEX IF NOT EXISTS idx_tour_culture_items_tour_id ON tour_culture_items(tour_id);
