-- Bảng leads - khách để lại thông tin quan tâm tour
CREATE TABLE IF NOT EXISTS leads (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(100),
    tour_id BIGINT REFERENCES tours(id),
    message TEXT,
    source VARCHAR(50),
    status VARCHAR(50),
    admin_note TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
