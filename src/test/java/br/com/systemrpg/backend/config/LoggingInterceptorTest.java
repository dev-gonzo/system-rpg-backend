package br.com.systemrpg.backend.config;

import br.com.systemrpg.backend.constants.SecurityConstants;
import br.com.systemrpg.backend.util.LoggingUtil;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para LoggingInterceptor.
 */
@ExtendWith(MockitoExtension.class)
class LoggingInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @InjectMocks
    private LoggingInterceptor loggingInterceptor;

    private Logger logger;
    private Logger loggingUtilLogger;
    private ListAppender<ILoggingEvent> listAppender;
    private ListAppender<ILoggingEvent> loggingUtilAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        loggingUtilLogger = (Logger) LoggerFactory.getLogger(LoggingUtil.class);
        loggingUtilAppender = new ListAppender<>();
        loggingUtilAppender.start();
        loggingUtilLogger.addAppender(loggingUtilAppender);
        
        // Clear MDC before each test
        MDC.clear();
    }

    @Test
    void preHandle_ShouldSetMDCAndReturnTrue() {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);

        // Act
        boolean result = loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertTrue(result);
        assertEquals("GET", MDC.get("method"));
        assertEquals("/api/test", MDC.get("uri"));
        assertEquals("192.168.1.1", MDC.get("remoteAddr"));
        verify(request).setAttribute(eq("startTime"), any(Instant.class));
    }

    @Test
    void preHandle_WithXForwardedFor_ShouldUseForwardedIP() {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");

        // Act
        boolean result = loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertTrue(result);
        assertEquals("10.0.0.1", MDC.get("remoteAddr"));
    }

    @Test
    void preHandle_WithXRealIP_ShouldUseRealIP() {
        // Arrange
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/users/1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("172.16.0.1");

        // Act
        boolean result = loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertTrue(result);
        assertEquals("172.16.0.1", MDC.get("remoteAddr"));
    }

    @Test
    void afterCompletion_WithSuccessfulRequest_ShouldLogInfo() {
        // Arrange
        Instant startTime = Instant.now().minusMillis(100);
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Assert
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Solicitação concluída com sucesso"));
        assertTrue(logEvent.getFormattedMessage().contains("GET /api/test"));
        assertTrue(logEvent.getFormattedMessage().contains("Status: 200"));
    }

    @Test
    void afterCompletion_WithErrorStatus_ShouldLogWarn() {
        // Arrange
        Instant startTime = Instant.now().minusMillis(50);
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(response.getStatus()).thenReturn(400);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Assert
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Solicitação concluída com status de erro"));
        assertTrue(logEvent.getFormattedMessage().contains("POST /api/users"));
        assertTrue(logEvent.getFormattedMessage().contains("Status: 400"));
    }

    @Test
    void afterCompletion_WithExceptionAndErrorStatus_ShouldLogError() {
        // Arrange
        Instant startTime = Instant.now().minusMillis(75);
        Exception testException = new RuntimeException("Test exception");
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/users/1");
        when(response.getStatus()).thenReturn(500);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, testException);

        // Assert
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.ERROR, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Solicitação concluída com erro"));
        assertTrue(logEvent.getFormattedMessage().contains("DELETE /api/users/1"));
        assertTrue(logEvent.getFormattedMessage().contains("Status: 500"));
        assertNotNull(logEvent.getThrowableProxy());
    }

    @Test
    void afterCompletion_WithSlowRequest_ShouldLogPerformanceAlert() {
        // Arrange
        Instant startTime = Instant.now().minusMillis(SecurityConstants.PERFORMANCE_THRESHOLD_MS + 100);
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/slow-endpoint");
        when(response.getStatus()).thenReturn(200);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Assert
        // Should have at least 1 log event for completion
        assertTrue(listAppender.list.size() >= 1);
        
        // Check if LoggingUtil logged the performance alert
        boolean hasPerformanceLog = loggingUtilAppender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains("Performance alert"));
        assertTrue(hasPerformanceLog);
    }

    @Test
    void afterCompletion_WithNullStartTime_ShouldHandleGracefully() {
        // Arrange
        when(request.getAttribute("startTime")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        // Act & Assert
        assertDoesNotThrow(() -> {
            loggingInterceptor.afterCompletion(request, response, handler, null);
        });

        // Should still log the completion
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertTrue(logEvent.getFormattedMessage().contains("Time: 0ms"));
    }

    @Test
    void getClientIpAddress_WithXForwardedFor_ShouldReturnFirstIP() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1, 172.16.0.1");

        // Act - test indirectly through preHandle
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertEquals("10.0.0.1", MDC.get("remoteAddr"));
    }

    @Test
    void getClientIpAddress_WithUnknownXForwardedFor_ShouldFallbackToXRealIP() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn("172.16.0.1");

        // Act - test indirectly through preHandle
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertEquals("172.16.0.1", MDC.get("remoteAddr"));
    }

    @Test
    void getClientIpAddress_WithEmptyHeaders_ShouldFallbackToRemoteAddr() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("X-Real-IP")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act - test indirectly through preHandle
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertEquals("192.168.1.1", MDC.get("remoteAddr"));
    }

    @Test
    void getClientIpAddress_WithUnknownXRealIP_ShouldFallbackToRemoteAddr() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act - test indirectly through preHandle
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertEquals("192.168.1.1", MDC.get("remoteAddr"));
    }

    @Test
    void constructor_ShouldCreateInstance() {
        // Act
        LoggingInterceptor interceptor = new LoggingInterceptor();

        // Assert
        assertNotNull(interceptor);
    }

    @Test
    void afterCompletion_ShouldClearMDCContext() {
        // Arrange
        MDC.put("testKey", "testValue");
        Instant startTime = Instant.now().minusMillis(50);
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Assert
        assertNull(MDC.get("testKey"));
        assertNull(MDC.get("method"));
        assertNull(MDC.get("uri"));
        assertNull(MDC.get("remoteAddr"));
    }
}
