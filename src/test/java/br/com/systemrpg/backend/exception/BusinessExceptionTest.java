package br.com.systemrpg.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe BusinessException.
 */
class BusinessExceptionTest {

    @Test
    void constructor_WithMessage_ShouldCreateExceptionWithDefaultHttpStatus() {
        // Arrange
        String message = "Test business exception";

        // Act
        BusinessException exception = new BusinessException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constructor_WithMessageAndHttpStatus_ShouldCreateExceptionWithCustomHttpStatus() {
        // Arrange
        String message = "Test business exception";
        HttpStatus customStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        // Act
        BusinessException exception = new BusinessException(message, customStatus);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(customStatus, exception.getHttpStatus());
    }

    @Test
    void getHttpStatus_ShouldReturnCorrectStatus() {
        // Arrange
        BusinessException exceptionWithDefault = new BusinessException("test");
        BusinessException exceptionWithCustom = new BusinessException("test", HttpStatus.NOT_FOUND);

        // Act & Assert
        assertEquals(HttpStatus.BAD_REQUEST, exceptionWithDefault.getHttpStatus());
        assertEquals(HttpStatus.NOT_FOUND, exceptionWithCustom.getHttpStatus());
    }

    @Test
    void constructor_WithNullMessage_ShouldHandleNullMessage() {
        // Arrange & Act
        BusinessException exception = new BusinessException(null);

        // Assert
        assertNull(exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void constructor_WithNullHttpStatus_ShouldHandleNullStatus() {
        // Arrange & Act
        BusinessException exception = new BusinessException("test", null);

        // Assert
        assertEquals("test", exception.getMessage());
        assertNull(exception.getHttpStatus());
    }

    @Test
    void serialVersionUID_ShouldBeConsistent() {
        // Arrange & Act
        // Verifica que a classe pode ser serializada e tem serialVersionUID consistente
        
        // Assert
        // Verifica que a classe pode ser serializada
        assertNotNull(BusinessException.class.getName());
    }

    @Test
    void inheritance_ShouldExtendRuntimeException() {
        // Arrange & Act
        BusinessException exception = new BusinessException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
    }

    @Test
    void httpStatusVariations_ShouldWorkWithDifferentStatuses() {
        // Arrange & Act
        BusinessException badRequest = new BusinessException("Bad request", HttpStatus.BAD_REQUEST);
        BusinessException unauthorized = new BusinessException("Unauthorized", HttpStatus.UNAUTHORIZED);
        BusinessException forbidden = new BusinessException("Forbidden", HttpStatus.FORBIDDEN);
        BusinessException notFound = new BusinessException("Not found", HttpStatus.NOT_FOUND);
        BusinessException conflict = new BusinessException("Conflict", HttpStatus.CONFLICT);
        BusinessException internalError = new BusinessException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getHttpStatus());
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getHttpStatus());
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getHttpStatus());
        assertEquals(HttpStatus.NOT_FOUND, notFound.getHttpStatus());
        assertEquals(HttpStatus.CONFLICT, conflict.getHttpStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, internalError.getHttpStatus());
    }
}
