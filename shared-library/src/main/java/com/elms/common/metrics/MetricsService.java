package com.elms.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * STORY 08 - Custom Metrics Service
 * Tracks business and technical metrics for observability
 * Integrates with Micrometer for exportable metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Leave Service Metrics
    public void recordLeaveRequested(String leaveType) {
        Counter.builder("leave.requested")
                .description("Number of leave requests")
                .tag("leave_type", leaveType)
                .register(meterRegistry)
                .increment();
    }

    public void recordLeaveApproved(String leaveType) {
        Counter.builder("leave.approved")
                .description("Number of approved leave requests")
                .tag("leave_type", leaveType)
                .register(meterRegistry)
                .increment();
    }

    public void recordLeaveRejected(String leaveType) {
        Counter.builder("leave.rejected")
                .description("Number of rejected leave requests")
                .tag("leave_type", leaveType)
                .register(meterRegistry)
                .increment();
    }

    // Employee Service Metrics
    public void recordEmployeeCreated() {
        Counter.builder("employee.created")
                .description("Number of employees created")
                .register(meterRegistry)
                .increment();
    }

    public void recordEmployeeUpdated() {
        Counter.builder("employee.updated")
                .description("Number of employee updates")
                .register(meterRegistry)
                .increment();
    }

    // Event Metrics
    public void recordEventPublished(String eventType) {
        Counter.builder("event.published")
                .description("Number of events published")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .increment();
    }

    public void recordEventProcessed(String eventType) {
        Counter.builder("event.processed")
                .description("Number of events processed")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .increment();
    }

    public void recordEventFailed(String eventType) {
        Counter.builder("event.failed")
                .description("Number of event processing failures")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .increment();
    }

    public void recordEventDead(String eventType) {
        Counter.builder("event.dead_lettered")
                .description("Number of events moved to DLQ")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .increment();
    }

    // Cache Metrics
    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hit")
                .description("Number of cache hits")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
    }

    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.miss")
                .description("Number of cache misses")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
    }

    // API Metrics
    public void recordApiRequest(String endpoint, String method) {
        Counter.builder("api.request")
                .description("Number of API requests")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry)
                .increment();
    }

    public void recordApiError(String endpoint, int statusCode) {
        Counter.builder("api.error")
                .description("Number of API errors")
                .tag("endpoint", endpoint)
                .tag("status_code", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }

    // Saga Metrics
    public void recordSagaStarted(String sagaType) {
        Counter.builder("saga.started")
                .description("Number of sagas started")
                .tag("saga_type", sagaType)
                .register(meterRegistry)
                .increment();
    }

    public void recordSagaCompleted(String sagaType) {
        Counter.builder("saga.completed")
                .description("Number of sagas completed")
                .tag("saga_type", sagaType)
                .register(meterRegistry)
                .increment();
    }

    public void recordSagaFailed(String sagaType) {
        Counter.builder("saga.failed")
                .description("Number of sagas failed")
                .tag("saga_type", sagaType)
                .register(meterRegistry)
                .increment();
    }

    public void recordSagaCompensated(String sagaType) {
        Counter.builder("saga.compensated")
                .description("Number of sagas with compensation")
                .tag("saga_type", sagaType)
                .register(meterRegistry)
                .increment();
    }

    // Lock Metrics
    public void recordLockAcquired(String resourceId) {
        Counter.builder("lock.acquired")
                .description("Number of distributed locks acquired")
                .tag("resource", resourceId)
                .register(meterRegistry)
                .increment();
    }

    public void recordLockTimeout(String resourceId) {
        Counter.builder("lock.timeout")
                .description("Number of lock acquisition timeouts")
                .tag("resource", resourceId)
                .register(meterRegistry)
                .increment();
    }

    // Timer for measuring operation duration
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordTimer(Timer.Sample sample, String name, String... tags) {
        Timer timer = Timer.builder(name)
                .description("Operation timer")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
        sample.stop(timer);
    }
}

