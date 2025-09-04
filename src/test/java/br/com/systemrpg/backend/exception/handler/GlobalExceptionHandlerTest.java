package br.com.systemrpg.backend.exception.handler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void handleAuthenticationException_WithAuthenticationException_ShouldReturnUnauthorized() {
        // Arrange
        AuthenticationException exception = new BadCredentialsException("Invalid credentials");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/login");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Credenciais inválidas", response.getBody().getMessage());
        assertEquals("/api/login", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());

        // Verify logging
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Authentication failed"));
        assertTrue(logEvent.getFormattedMessage().contains("Invalid credentials"));
    }

    @Test
    void handleAuthenticationException_WithBadCredentialsException_ShouldReturnUnauthorized() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/auth");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Credenciais inválidas", response.getBody().getMessage());
        assertEquals("/api/auth", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());

        // Verify logging
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Authentication failed"));
        assertTrue(logEvent.getFormattedMessage().contains("Bad credentials"));
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbidden() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access is denied");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/admin");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Acesso negado", response.getBody().getMessage());
        assertEquals("/api/admin", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());

        // Verify logging
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Access denied"));
        assertTrue(logEvent.getFormattedMessage().contains("Access is denied"));
    }

    @Test
    void handleNoResourceFoundException_ShouldReturnNotFound() {
        // Arrange
        NoResourceFoundException exception = new NoResourceFoundException(null, "/nonexistent");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/nonexistent");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleNoResourceFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Recurso não encontrado", response.getBody().getMessage());
        assertEquals("/api/nonexistent", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());

        // Verify logging
        assertEquals(1, listAppender.list.size());
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("Resource not found"));
    }

    @Test
    void getPath_ShouldExtractPathFromWebRequest() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test/path");
        AuthenticationException exception = new BadCredentialsException("Test");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertEquals("/api/test/path", response.getBody().getPath());
    }

    @Test
    void getPath_WithShortDescription_ShouldHandleGracefully() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=");
        AuthenticationException exception = new BadCredentialsException("Test");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertEquals("", response.getBody().getPath());
    }

    @Test
    void constructor_ShouldCreateInstance() {
        // Act
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Assert
        assertNotNull(handler);
    }

    @Test
    void handleAuthenticationException_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        AuthenticationException exception = new AuthenticationException(null) {};
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciais inválidas", response.getBody().getMessage());
    }

    @Test
    void handleAccessDeniedException_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException(null);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Acesso negado", response.getBody().getMessage());
    }

    @Test
    void handleNoResourceFoundException_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        NoResourceFoundException exception = new NoResourceFoundException(null, null);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

        // Act
        ResponseEntity<RestResponse> response = globalExceptionHandler.handleNoResourceFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso não encontrado", response.getBody().getMessage());
    }

    @Test
    void allHandlers_ShouldReturnRestResponseWithCorrectStructure() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
        
        AuthenticationException authException = new BadCredentialsException("Auth error");
        AccessDeniedException accessException = new AccessDeniedException("Access error");
        NoResourceFoundException notFoundException = new NoResourceFoundException(null, "/test");

        // Act & Assert
        ResponseEntity<RestResponse> authResponse = globalExceptionHandler.handleAuthenticationException(authException, webRequest);
        ResponseEntity<RestResponse> accessResponse = globalExceptionHandler.handleAccessDeniedException(accessException, webRequest);
        ResponseEntity<RestResponse> notFoundResponse = globalExceptionHandler.handleNoResourceFoundException(notFoundException, webRequest);

        // Verify all responses have the expected structure
        assertNotNull(authResponse.getBody().getTimestamp());
        assertTrue(authResponse.getBody().getStatus() > 0);
        assertNotNull(authResponse.getBody().getError());
        assertNotNull(authResponse.getBody().getMessage());
        assertNotNull(authResponse.getBody().getPath());
        assertNotNull(authResponse.getBody().getFieldErrors());

        assertNotNull(accessResponse.getBody().getTimestamp());
        assertTrue(accessResponse.getBody().getStatus() > 0);
        assertNotNull(accessResponse.getBody().getError());
        assertNotNull(accessResponse.getBody().getMessage());
        assertNotNull(accessResponse.getBody().getPath());
        assertNotNull(accessResponse.getBody().getFieldErrors());

        assertNotNull(notFoundResponse.getBody().getTimestamp());
        assertTrue(notFoundResponse.getBody().getStatus() > 0);
        assertNotNull(notFoundResponse.getBody().getError());
        assertNotNull(notFoundResponse.getBody().getMessage());
        assertNotNull(notFoundResponse.getBody().getPath());
        assertNotNull(notFoundResponse.getBody().getFieldErrors());
    }
}
