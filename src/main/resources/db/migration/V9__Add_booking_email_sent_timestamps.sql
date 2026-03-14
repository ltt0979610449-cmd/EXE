-- Booking: Thêm cột theo dõi đã gửi email nhắc lịch và xin feedback
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS pre_departure_email_sent_at TIMESTAMP;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS post_tour_feedback_email_sent_at TIMESTAMP;
