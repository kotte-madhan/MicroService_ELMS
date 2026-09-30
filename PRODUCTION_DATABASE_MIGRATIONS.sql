-- Production Database Migration Scripts
-- Run these scripts on each service database

-- ===================================================
-- STORY 1: Outbox Events Table
-- ===================================================

CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    error_message TEXT,
    correlation_id VARCHAR(255)
);

CREATE INDEX idx_outbox_status ON outbox_events(status);
CREATE INDEX idx_outbox_created ON outbox_events(created_at);
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_id);
CREATE INDEX idx_outbox_correlation ON outbox_events(correlation_id);

-- ===================================================
-- STORY 3: Processed Events Table
-- ===================================================

CREATE TABLE IF NOT EXISTS processed_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    consumer VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(255),
    payload TEXT,
    UNIQUE(event_id, consumer)
);

CREATE INDEX idx_processed_event ON processed_events(event_id, consumer);
CREATE INDEX idx_processed_created ON processed_events(processed_at);
CREATE INDEX idx_processed_consumer ON processed_events(consumer);

-- ===================================================
-- STORY 4: Saga Execution Tables
-- ===================================================

CREATE TABLE IF NOT EXISTS saga_executions (
    id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(255) UNIQUE NOT NULL,
    saga_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_step INTEGER NOT NULL,
    total_steps INTEGER NOT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    compensation_started_at TIMESTAMP,
    compensation_completed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    error_message TEXT,
    correlation_id VARCHAR(255)
);

CREATE INDEX idx_saga_status ON saga_executions(status);
CREATE INDEX idx_saga_aggregate ON saga_executions(aggregate_id);
CREATE INDEX idx_saga_created ON saga_executions(created_at);
CREATE INDEX idx_saga_type ON saga_executions(saga_type);

CREATE TABLE IF NOT EXISTS saga_step_executions (
    id BIGSERIAL PRIMARY KEY,
    saga_id BIGINT NOT NULL,
    step_number INTEGER NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    compensation_action VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    response_data TEXT,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (saga_id) REFERENCES saga_executions(id) ON DELETE CASCADE
);

CREATE INDEX idx_saga_step_saga ON saga_step_executions(saga_id);
CREATE INDEX idx_saga_step_status ON saga_step_executions(status);
CREATE INDEX idx_saga_step_service ON saga_step_executions(service_name);

-- ===================================================
-- Employee Table Indexes (STORY 11)
-- ===================================================

CREATE INDEX IF NOT EXISTS idx_employee_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employee_code ON employees(employee_code);
CREATE INDEX IF NOT EXISTS idx_employee_department ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_employee_status ON employees(status);

-- ===================================================
-- Leave Table Indexes (STORY 11)
-- ===================================================

CREATE INDEX IF NOT EXISTS idx_leave_employee ON leave_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_leave_status ON leave_requests(status);
CREATE INDEX IF NOT EXISTS idx_leave_start_date ON leave_requests(start_date);
CREATE INDEX IF NOT EXISTS idx_leave_dates ON leave_requests(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_leave_employee_status ON leave_requests(employee_id, status);
CREATE INDEX IF NOT EXISTS idx_leave_approved_date ON leave_requests(approved_date)
    WHERE status = 'APPROVED';

-- ===================================================
-- Audit Table (Optional but Recommended)
-- ===================================================

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id VARCHAR(255),
    old_values TEXT,
    new_values TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log(entity_type, entity_id);

-- ===================================================
-- DLQ Table (for analyzing failed messages)
-- ===================================================

CREATE TABLE IF NOT EXISTS dlq_messages (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    event_id VARCHAR(255),
    message_body TEXT NOT NULL,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_dlq_event_type ON dlq_messages(event_type);
CREATE INDEX IF NOT EXISTS idx_dlq_received ON dlq_messages(received_at);
CREATE INDEX IF NOT EXISTS idx_dlq_resolved ON dlq_messages(resolved_at)
    WHERE resolved_at IS NULL;

-- ===================================================
-- Verify Table Creation
-- ===================================================

-- Run this to verify all tables were created:
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'public'
-- ORDER BY table_name;

-- ===================================================
-- Create Sequences (if not auto-created)
-- ===================================================

-- PostgreSQL usually auto-creates sequences, but verify:
-- SELECT sequence_name FROM information_schema.sequences
-- WHERE sequence_schema = 'public';

