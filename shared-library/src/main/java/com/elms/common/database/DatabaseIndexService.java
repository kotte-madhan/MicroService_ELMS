package com.elms.common.database;

import  lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * STORY 11 - Database Index Documentation
 * This service documents the database indexes that should be created
 * Indexes are critical for query performance and should be created explicitly
 *
 * IMPORTANT: Run these SQL commands on each database to create indexes:
 *
 * === EMPLOYEE SERVICE ===
 * CREATE INDEX idx_employee_email ON employees(email);
 * CREATE INDEX idx_employee_code ON employees(employee_code);
 * CREATE INDEX idx_employee_department ON employees(department_id);
 * CREATE INDEX idx_employee_status ON employees(status);
 *
 * === LEAVE SERVICE ===
 * CREATE INDEX idx_leave_employee ON leave_requests(employee_id);
 * CREATE INDEX idx_leave_status ON leave_requests(status);
 * CREATE INDEX idx_leave_start_date ON leave_requests(start_date);
 * CREATE INDEX idx_leave_dates ON leave_requests(start_date, end_date);
 * CREATE INDEX idx_leave_approval ON leave_requests(approved_date) WHERE status = 'APPROVED';
 *
 * === SHARED (Outbox & Events) ===
 * CREATE INDEX idx_outbox_status ON outbox_events(status);
 * CREATE INDEX idx_outbox_created ON outbox_events(created_at);
 * CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_id);
 * CREATE INDEX idx_processed_event ON processed_events(event_id, consumer);
 * CREATE INDEX idx_processed_created ON processed_events(processed_at);
 *
 * === AUDIT & MONITORING ===
 * CREATE INDEX idx_audit_created ON audit_log(created_at);
 * CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
 *
 * === SAGA ===
 * CREATE INDEX idx_saga_status ON saga_executions(status);
 * CREATE INDEX idx_saga_aggregate ON saga_executions(aggregate_id);
 * CREATE INDEX idx_saga_created ON saga_executions(created_at);
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.database.index-check.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseIndexService {

    /**
     * Documentation of query optimization patterns
     */
    public static class QueryOptimizationPatterns {

        /**
         * QUERY 1: Find employee by email (frequent lookup)
         * SELECT * FROM employees WHERE email = ?
         * INDEX NEEDED: idx_employee_email
         * IMPACT: Reduces full table scan
         */
        public static final String EMPLOYEE_BY_EMAIL_QUERY =
                "SELECT * FROM employees WHERE email = ?";

        /**
         * QUERY 2: Find leave requests for employee
         * SELECT * FROM leave_requests WHERE employee_id = ? AND status = ?
         * INDEX NEEDED: idx_leave_employee, idx_leave_status (composite preferred)
         * IMPACT: Used in dashboard, approvals, balance calculations
         */
        public static final String LEAVE_BY_EMPLOYEE_QUERY =
                "SELECT * FROM leave_requests WHERE employee_id = ? AND status = ?";

        /**
         * QUERY 3: Find outbox events pending publication
         * SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at
         * INDEX NEEDED: idx_outbox_status
         * IMPACT: Periodic background job runs every 5 seconds
         */
        public static final String OUTBOX_PENDING_QUERY =
                "SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at";

        /**
         * QUERY 4: Check if event already processed
         * SELECT * FROM processed_events WHERE event_id = ? AND consumer = ?
         * INDEX NEEDED: idx_processed_event (composite unique key)
         * IMPACT: Duplicate detection on every event received
         */
        public static final String DUPLICATE_CHECK_QUERY =
                "SELECT * FROM processed_events WHERE event_id = ? AND consumer = ?";

        /**
         * QUERY 5: Find leave dates overlap
         * SELECT * FROM leave_requests WHERE employee_id = ?
         *   AND NOT (end_date < ? OR start_date > ?)
         * INDEX NEEDED: idx_leave_dates (start_date, end_date)
         * IMPACT: Validation during leave application
         */
        public static final String LEAVE_DATE_OVERLAP_QUERY =
                "SELECT * FROM leave_requests WHERE employee_id = ? " +
                "AND NOT (end_date < ? OR start_date > ?)";
    }

    /**
     * Performance metrics showing index effectiveness
     * Run: SELECT * FROM pg_stat_user_indexes;
     */
    public static class PerformanceMetrics {
        public static final String QUERY_WITH_INDEX =
                "SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public'";

        public static final String TABLE_BLOAT =
                "SELECT * FROM pg_stat_user_tables WHERE schemaname = 'public'";
    }
}

