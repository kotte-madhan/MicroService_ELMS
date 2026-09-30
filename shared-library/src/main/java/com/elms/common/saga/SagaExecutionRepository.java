package com.elms.common.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * STORY 04 - Saga Execution Repository
 */
@Repository
public interface SagaExecutionRepository extends JpaRepository<SagaExecution, Long> {

    /**
     * Find saga by ID
     */
    Optional<SagaExecution> findBySagaId(String sagaId);

    /**
     * Find sagas by aggregate ID
     */
    List<SagaExecution> findByAggregateId(String aggregateId);

    /**
     * Find incomplete sagas that need recovery
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.status IN ('STARTED', 'EMPLOYEE_VALIDATED', 'BALANCE_CHECKED', 'LEAVE_CREATED', 'BALANCE_UPDATED', 'EVENT_PUBLISHED', 'FAILED', 'COMPENSATING')")
    List<SagaExecution> findIncompleteExecutions();

    /**
     * Find permanently failed sagas
     */
    List<SagaExecution> findByStatus(SagaExecution.SagaStatus status);
}

