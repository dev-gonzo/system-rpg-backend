package br.com.systemrpg.backend.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe AvailabilityResponse.
 */
class AvailabilityResponseTest {

    @Test
    void constructor_NoArgs_ShouldCreateObjectWithDefaultValues() {
        // Act
        AvailabilityResponse response = new AvailabilityResponse();

        // Assert
        assertNotNull(response);
        assertFalse(response.isAvailable());
    }

    @Test
    void constructor_WithTrue_ShouldCreateObjectWithAvailableTrue() {
        // Act
        AvailabilityResponse response = new AvailabilityResponse(true);

        // Assert
        assertNotNull(response);
        assertTrue(response.isAvailable());
    }

    @Test
    void constructor_WithFalse_ShouldCreateObjectWithAvailableFalse() {
        // Act
        AvailabilityResponse response = new AvailabilityResponse(false);

        // Assert
        assertNotNull(response);
        assertFalse(response.isAvailable());
    }

    @Test
    void setAvailable_WithTrue_ShouldSetAvailableToTrue() {
        // Arrange
        AvailabilityResponse response = new AvailabilityResponse();

        // Act
        response.setAvailable(true);

        // Assert
        assertTrue(response.isAvailable());
    }

    @Test
    void setAvailable_WithFalse_ShouldSetAvailableToFalse() {
        // Arrange
        AvailabilityResponse response = new AvailabilityResponse(true);

        // Act
        response.setAvailable(false);

        // Assert
        assertFalse(response.isAvailable());
    }

    @Test
    void isAvailable_ShouldReturnCurrentValue() {
        // Arrange
        AvailabilityResponse response = new AvailabilityResponse();

        // Act & Assert - initial value
        assertFalse(response.isAvailable());

        // Act & Assert - after setting to true
        response.setAvailable(true);
        assertTrue(response.isAvailable());

        // Act & Assert - after setting to false
        response.setAvailable(false);
        assertFalse(response.isAvailable());
    }
}
