package com.br.org.icol.icolbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger("HTTP_REQUESTS");
    private static final String START_NANOS_ATTRIBUTE =
            HttpLoggingInterceptor.class.getName() + ".startNanos";
    private static final Pattern CPF_PATTERN =
            Pattern.compile("(?<!\\d)(?:\\d[.\\-]?){10}\\d(?!\\d)");

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        request.setAttribute(START_NANOS_ATTRIBUTE, System.nanoTime());
        String uri = sanitizeUri(request.getRequestURI());
        String query = request.getQueryString() == null ? "" : "?[REDACTED]";

        logger.info(
                "[{}] {}{} - Remote IP: {}",
                request.getMethod(),
                uri,
                query,
                request.getRemoteAddr()
        );

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        Object startNanos = request.getAttribute(START_NANOS_ATTRIBUTE);
        if (!(startNanos instanceof Long startedAt)) {
            logger.warn("Request start time was not recorded for {}", sanitizeUri(request.getRequestURI()));
            return;
        }

        long responseTimeMillis = (System.nanoTime() - startedAt) / 1_000_000;
        logger.info(
                "[{}] {} - Status: {} - Response Time: {} ms",
                request.getMethod(),
                sanitizeUri(request.getRequestURI()),
                response.getStatus(),
                responseTimeMillis
        );
    }

    private String sanitizeUri(String uri) {
        String normalizedUri = uri
                .replaceAll("(?i)%2e", ".")
                .replaceAll("(?i)%2d", "-");
        return CPF_PATTERN.matcher(normalizedUri).replaceAll("[CPF_REDACTED]");
    }
}
