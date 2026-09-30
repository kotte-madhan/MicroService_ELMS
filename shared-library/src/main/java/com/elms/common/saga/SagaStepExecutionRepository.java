package com.elms.common.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * STORY 04 - Saga Step Execution Repository
 */
@Repository
public interface SagaStepExecutionRepository extends JpaRepository<SagaStepExecution, Long> {

    /**
     * Find all steps for a saga
     */
    List<SagaStepExecution> findBySagaExecutionOrderByStepNumberAsc(SagaExecution sagaExecution);

    /**
     * Find steps by status
     */
    List<SagaStepExecution> findBySagaExecutionAndStatus(SagaExecution sagaExecution, SagaStepExecution.StepStatus status);

    /**
     * Find failed steps
     */
    List<SagaStepExecution> findBySagaExecutionAndStatusAndIsRequiredTrue(SagaExecution sagaExecution, SagaStepExecution.StepStatus status);
}

