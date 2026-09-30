package com.elms.common.idempotency;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity for storing idempotency keys and their responses
 * Prevents duplicate operations when the same request is sent multiple times
 */
@Entity
@Table(name = "idempotency_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey implements Serializable {

    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "method")
    private String method;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "request_hash")
    private String requestHash;
}

