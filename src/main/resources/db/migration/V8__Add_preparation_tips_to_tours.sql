-- Tour: Thêm preparationTips (lưu ý chuẩn bị trang phục, đồ dùng)
ALTER TABLE tours ADD COLUMN IF NOT EXISTS preparation_tips TEXT;
