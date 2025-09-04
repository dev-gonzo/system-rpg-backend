package br.com.systemrpg.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Utilitário para gerenciamento de logs e contexto de requisições.
 */
@Component
public class LoggingUtil {

    private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);

    private LoggingUtil() {
        // Private constructor to hide the implicit public one
    }

    public static final String REQUEST_ID = "requestId";
    public static final String EXECUTION_TIME = "executionTime";

    public static void clearContext() {
        String requestId = MDC.get(REQUEST_ID);
        if (requestId != null) {
            log.info("Solicitação concluída com ID: {}", requestId);
        }
        MDC.clear();
    }

    public static void performanceLog(String operation, long executionTime, int threshold) {
        if (executionTime > threshold) {
            log.warn("Performance alert: {} took {}ms (threshold: {}ms)", operation, executionTime, threshold);
        }
    }

}
