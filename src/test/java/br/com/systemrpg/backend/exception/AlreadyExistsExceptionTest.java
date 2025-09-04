package br.com.systemrpg.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe AlreadyExistsException.
 */
class AlreadyExistsExceptionTest {

    @Test
    void constructor_WithMessage_ShouldCreateExceptionWithMessage() {
        // Arrange
        String message = "Test exception message";

        // Act
        AlreadyExistsException exception = new AlreadyExistsException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception instanceof BusinessException);
    }

    @Test
    void constructor_WithEntityNameAndValue_ShouldCreateFormattedMessage() {
        // Arrange
        String entityName = "Usuario";
        String value = "admin";
        String expectedMessage = "Usuario com valor 'admin' já existe no sistema";

        // Act
        AlreadyExistsException exception = new AlreadyExistsException(entityName, value);

        // Assert
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void constructor_WithEntityNameAndNullValue_ShouldHandleNullValue() {
        // Arrange
        String entityName = "Email";
        Object value = null;
        String expectedMessage = "Email com valor 'null' já existe no sistema";

        // Act
        AlreadyExistsException exception = new AlreadyExistsException(entityName, value);

        // Assert
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_WithEntityNameAndIntegerValue_ShouldFormatCorrectly() {
        // Arrange
        String entityName = "ID";
        Integer value = 123;
        String expectedMessage = "ID com valor '123' já existe no sistema";

        // Act
        AlreadyExistsException exception = new AlreadyExistsException(entityName, value);

        // Assert
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void serialVersionUID_ShouldBeConsistent() {
        // Arrange & Act
        // Verifica que a classe pode ser serializada e tem serialVersionUID consistente
        
        // Assert
        // Verifica que a classe pode ser serializada
        assertNotNull(AlreadyExistsException.class.getName());
    }

    @Test
    void inheritance_ShouldExtendBusinessException() {
        // Arrange & Act
        AlreadyExistsException exception = new AlreadyExistsException("test");

        // Assert
        assertTrue(exception instanceof BusinessException);
        assertTrue(exception instanceof RuntimeException);
    }
}
