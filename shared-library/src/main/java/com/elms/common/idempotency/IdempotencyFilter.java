package com.elms.common.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Idempotency Filter
 * Ensures that requests with the same idempotency key return the same response
 * Prevents duplicate operations from being processed multiple times
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final long IDEMPOTENCY_TTL_HOURS = 24; // Keep idempotency keys for 24 hours

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        // Only process requests with idempotency key
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {

            // Check if this is a mutation request (POST, PUT, PATCH, DELETE)
            String method = request.getMethod();
            if (isMutationRequest(method)) {

                LocalDateTime now = LocalDateTime.now();
                Optional<IdempotencyKey> existingKey =
                        idempotencyKeyRepository.findByIdempotencyKeyAndExpiresAtAfter(
                                idempotencyKey, now);

                if (existingKey.isPresent()) {
                    // Return the cached response
                    IdempotencyKey cachedResponse = existingKey.get();
                    log.info("Returning cached response for idempotency key: {}", idempotencyKey);

                    response.setStatus(cachedResponse.getStatusCode());
                    response.setContentType("application/json");
                    response.getWriter().write(cachedResponse.getResponse());
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request is a mutation request
     */
    private boolean isMutationRequest(String method) {
        return method.equalsIgnoreCase("POST") ||
               method.equalsIgnoreCase("PUT") ||
               method.equalsIgnoreCase("PATCH") ||
               method.equalsIgnoreCase("DELETE");
    }

    /**
     * Generate hash of request body for duplicate detection
     */
    public static String generateRequestHash(String requestBody) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(requestBody.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to generate request hash", e);
            return null;
        }
    }
}

