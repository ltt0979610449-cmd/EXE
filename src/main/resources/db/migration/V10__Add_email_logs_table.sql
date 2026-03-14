-- Bảng lưu lịch sử gửi email và tracking
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(255),
    subject VARCHAR(500),
    template_type VARCHAR(100),
    related_id BIGINT,
    related_type VARCHAR(50),
    status VARCHAR(20),
    sent_at TIMESTAMP,
    opened_at TIMESTAMP,
    opened_count INTEGER DEFAULT 0,
    created_at TIMESTAMP
);
