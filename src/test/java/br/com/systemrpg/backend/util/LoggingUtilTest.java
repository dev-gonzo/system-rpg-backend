package br.com.systemrpg.backend.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class LoggingUtilTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingUtil.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        // Clear MDC before each test
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        MDC.clear();
    }

    @Test
    void clearContext_WithRequestId_ShouldLogCompletionAndClearMDC() {
        String requestId = "test-request-123";
        MDC.put(LoggingUtil.REQUEST_ID, requestId);
        MDC.put("otherKey", "otherValue");
        
        LoggingUtil.clearContext();
        
        // Verify MDC is cleared
        assertNull(MDC.get(LoggingUtil.REQUEST_ID));
        assertNull(MDC.get("otherKey"));
        
        // Verify log message
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Solicitação concluída com ID: " + requestId));
    }

    @Test
    void clearContext_WithoutRequestId_ShouldOnlyClearMDC() {
        MDC.put("someKey", "someValue");
        
        LoggingUtil.clearContext();
        
        // Verify MDC is cleared
        assertNull(MDC.get("someKey"));
        
        // Verify no log message was generated
        assertEquals(0, listAppender.list.size());
    }

    @Test
    void clearContext_WithEmptyMDC_ShouldNotThrowException() {
        assertDoesNotThrow(LoggingUtil::clearContext);
        
        // Verify no log message was generated
        assertEquals(0, listAppender.list.size());
    }

    @ParameterizedTest
    @CsvSource({
        "testOperation, 1500, 1000, 1, true",
        "fastOperation, 500, 1000, 0, false",
        "exactOperation, 1000, 1000, 0, false",
        "instantOperation, 0, 100, 0, false"
    })
    void performanceLog_ShouldLogBasedOnThreshold(String operation, long executionTime, int threshold, int expectedLogCount, boolean shouldContainAlert) {
        LoggingUtil.performanceLog(operation, executionTime, threshold);
        
        assertEquals(expectedLogCount, listAppender.list.size());
        
        if (shouldContainAlert) {
            ILoggingEvent logEvent = listAppender.list.get(0);
            assertEquals(Level.WARN, logEvent.getLevel());
            assertTrue(logEvent.getFormattedMessage().contains("Performance alert"));
            assertTrue(logEvent.getFormattedMessage().contains(operation));
            assertTrue(logEvent.getFormattedMessage().contains(String.valueOf(executionTime)));
            assertTrue(logEvent.getFormattedMessage().contains(String.valueOf(threshold)));
        }
    }

    @Test
    void performanceLog_WithNegativeThreshold_ShouldLogWarning() {
        String operation = "testOperation";
        long executionTime = 100L;
        int threshold = -1;
        
        LoggingUtil.performanceLog(operation, executionTime, threshold);
        
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, logEvent.getLevel());
    }

    @Test
    void constants_ShouldHaveCorrectValues() {
        assertEquals("requestId", LoggingUtil.REQUEST_ID);
        assertEquals("executionTime", LoggingUtil.EXECUTION_TIME);
    }
}
