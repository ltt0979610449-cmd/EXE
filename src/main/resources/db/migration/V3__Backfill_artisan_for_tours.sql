-- Chuẩn hóa artisanId: Gán nghệ nhân mặc định cho các tour chưa có artisan
-- Chỉ cập nhật tours có province_id và artisan_id IS NULL
-- Gán artisan đầu tiên (theo province) của cùng tỉnh

UPDATE tours t
SET artisan_id = (
    SELECT a.id
    FROM artisans a
    WHERE a.province_id = t.province_id
      AND (a.is_active IS NULL OR a.is_active = true)
    ORDER BY a.id ASC
    LIMIT 1
)
WHERE t.artisan_id IS NULL
  AND t.province_id IS NOT NULL
  AND EXISTS (
      SELECT 1 FROM artisans a
      WHERE a.province_id = t.province_id
        AND (a.is_active IS NULL OR a.is_active = true)
  );
