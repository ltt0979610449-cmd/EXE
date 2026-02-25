-- Thêm STAFF vào check constraint của cột role trong bảng users
-- Constraint cũ chỉ cho phép CUSTOMER, ARTISAN, ADMIN

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('CUSTOMER', 'ARTISAN', 'STAFF', 'ADMIN'));
