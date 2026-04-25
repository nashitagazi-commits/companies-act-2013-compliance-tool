-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Compliance records table
CREATE TABLE IF NOT EXISTS compliance_records (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    section_number VARCHAR(50),
    compliance_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    assigned_to VARCHAR(100),
    company_name VARCHAR(200) NOT NULL,
    filing_frequency VARCHAR(50),
    penalty_amount DOUBLE PRECISION,
    remarks TEXT,
    ai_description TEXT,
    ai_recommendations TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(20) NOT NULL,
    performed_by VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_compliance_status
    ON compliance_records(status);
CREATE INDEX IF NOT EXISTS idx_compliance_company
    ON compliance_records(company_name);
CREATE INDEX IF NOT EXISTS idx_compliance_due_date
    ON compliance_records(due_date);
CREATE INDEX IF NOT EXISTS idx_compliance_deleted
    ON compliance_records(is_deleted);
CREATE INDEX IF NOT EXISTS idx_audit_entity
    ON audit_logs(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_users_email
    ON users(email);