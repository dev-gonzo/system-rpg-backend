package br.com.systemrpg.backend.config;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.systemrpg.backend.util.LoggingUtil;
import br.com.systemrpg.backend.constants.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptador responsável por registrar logs de requisições HTTP.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("remoteAddr", getClientIpAddress(request));

        request.setAttribute(START_TIME_ATTRIBUTE, Instant.now());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            Instant startTime = (Instant) request.getAttribute(START_TIME_ATTRIBUTE);
            long executionTime = 0;

            if (startTime != null) {
                executionTime = java.time.Duration.between(startTime, Instant.now()).toMillis();
                MDC.put(LoggingUtil.EXECUTION_TIME, executionTime + "ms");
            }

            int status = response.getStatus();
            if (status >= SecurityConstants.HTTP_BAD_REQUEST) {
                if (ex != null) {
                    log.error("Solicitação concluída com erro: {} {} - Status: {} - Time: {}ms",
                            request.getMethod(), request.getRequestURI(), status, executionTime, ex);
                } else {
                    log.warn("Solicitação concluída com status de erro: {} {} - Status: {} - Time: {}ms",
                            request.getMethod(), request.getRequestURI(), status, executionTime);
                }
            } else {
                log.info("Solicitação concluída com sucesso: {} {} - Status: {} - Time: {}ms",
                        request.getMethod(), request.getRequestURI(), status, executionTime);
            }

            if (executionTime > SecurityConstants.PERFORMANCE_THRESHOLD_MS) {
                LoggingUtil.performanceLog(request.getMethod() + " " + request.getRequestURI(), executionTime, (int) SecurityConstants.PERFORMANCE_THRESHOLD_MS);
            }

        } finally {
            LoggingUtil.clearContext();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
