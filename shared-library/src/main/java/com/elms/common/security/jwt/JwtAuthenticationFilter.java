package com.elms.common.security.jwt;

import com.elms.common.exception.UnauthorizedException;
import com.elms.common.util.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT Authentication Filter
 * Validates JWT tokens in request headers and sets authentication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String correlationId = request.getHeader("X-Correlation-ID");
            if (correlationId == null || correlationId.isEmpty()) {
                response.setHeader("X-Correlation-ID", CorrelationIdUtil.generateCorrelationId());
            }

            String token = getTokenFromRequest(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"))
                );

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT Token validated for user: {} with role: {}", userId, role);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("JWT Authentication failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

