package com.elms.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider for generating and validating JWT tokens
 * Used for inter-service communication authentication
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:ElmsSecretKeyForJWTValidationAcrossAllMicroservices2026}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}") // Default: 1 hour
    private long jwtExpirationMs;

    /**
     * Generate JWT token for a user
     */
    public String generateToken(String userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        claims.put("userId", userId);

        return createToken(claims, userId);
    }

    /**
     * Generate Internal Service Token (for service-to-service communication)
     */
    public String generateServiceToken(String serviceName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "SERVICE");
        claims.put("serviceName", serviceName);

        return createToken(claims, serviceName);
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extract claims from token
     */
    public Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            log.error("Failed to extract claims from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Get user ID from token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("userId") : null;
    }

    /**
     * Get role from token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("email") : null;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null && claims.getExpiration().before(new Date());
    }
}

