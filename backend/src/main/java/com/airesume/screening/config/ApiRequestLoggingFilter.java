package com.airesume.screening.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs API requests and failures to application.log (especially exports and uploads).
 */
@Component
@Order(20)
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        boolean interesting = uri.contains("/reports")
                || uri.contains("/resumes/upload")
                || (uri.contains("/candidates")
                && ("POST".equals(method) || "DELETE".equals(method)));
        long start = System.currentTimeMillis();
        if (interesting) {
            log.info(">>> {} {}", method, uri);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (interesting) {
                long ms = System.currentTimeMillis() - start;
                int status = response.getStatus();
                if (status >= 400) {
                    log.warn("<<< {} {} -> {} ({} ms)", method, uri, status, ms);
                } else {
                    log.info("<<< {} {} -> {} ({} ms)", method, uri, status, ms);
                }
            }
        }
    }
}
