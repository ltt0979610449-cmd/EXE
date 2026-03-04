-- Link voucher to tour schedule for low-booking discount vouchers
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS tour_schedule_id BIGINT REFERENCES tour_schedules(id);
