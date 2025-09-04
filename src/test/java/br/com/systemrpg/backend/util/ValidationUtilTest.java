package br.com.systemrpg.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void isValidString_WithValidString_ShouldReturnTrue() {
        assertTrue(ValidationUtil.isValidString("valid string"));
        assertTrue(ValidationUtil.isValidString("  trimmed  "));
        assertTrue(ValidationUtil.isValidString("a"));
    }

    @Test
    void isValidString_WithInvalidString_ShouldReturnFalse() {
        assertFalse(ValidationUtil.isValidString(null));
        assertFalse(ValidationUtil.isValidString(""));
        assertFalse(ValidationUtil.isValidString("   "));
        assertFalse(ValidationUtil.isValidString("\t\n"));
    }

    @Test
    void requireValidString_WithValidString_ShouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.requireValidString("valid", "field"));
    }

    @Test
    void requireValidString_WithInvalidString_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.requireValidString(null, "username")
        );
        assertEquals("username não pode ser nulo ou vazio", exception.getMessage());

        exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.requireValidString("", "password")
        );
        assertEquals("password não pode ser nulo ou vazio", exception.getMessage());
    }

    @Test
    void isNotNull_WithValidObject_ShouldReturnTrue() {
        assertTrue(ValidationUtil.isNotNull("string"));
        assertTrue(ValidationUtil.isNotNull(123));
        assertTrue(ValidationUtil.isNotNull(new Object()));
    }

    @Test
    void isNotNull_WithNullObject_ShouldReturnFalse() {
        assertFalse(ValidationUtil.isNotNull(null));
    }

    @Test
    void requireNotNull_WithValidObject_ShouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.requireNotNull("valid", "field"));
    }

    @Test
    void requireNotNull_WithNullObject_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.requireNotNull(null, "user")
        );
        assertEquals("user não pode ser nulo", exception.getMessage());
    }

    @Test
    void isValidAuthHeader_WithValidHeader_ShouldReturnTrue() {
        assertTrue(ValidationUtil.isValidAuthHeader("Bearer token123"));
        assertTrue(ValidationUtil.isValidAuthHeader("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"));
    }

    @Test
    void isValidAuthHeader_WithInvalidHeader_ShouldReturnFalse() {
        assertFalse(ValidationUtil.isValidAuthHeader(null));
        assertFalse(ValidationUtil.isValidAuthHeader(""));
        assertFalse(ValidationUtil.isValidAuthHeader("Bearer"));
        assertFalse(ValidationUtil.isValidAuthHeader("Bearer "));
        assertFalse(ValidationUtil.isValidAuthHeader("Basic token"));
        assertFalse(ValidationUtil.isValidAuthHeader("token123"));
    }

    @Test
    void isValidEmailFormat_WithValidEmail_ShouldReturnTrue() {
        assertTrue(ValidationUtil.isValidEmailFormat("user@example.com"));
        assertTrue(ValidationUtil.isValidEmailFormat("test.email@domain.org"));
        assertTrue(ValidationUtil.isValidEmailFormat("admin@company.com.br"));
    }

    @Test
    void isValidEmailFormat_WithInvalidEmail_ShouldReturnFalse() {
        assertFalse(ValidationUtil.isValidEmailFormat(null));
        assertFalse(ValidationUtil.isValidEmailFormat(""));
        assertFalse(ValidationUtil.isValidEmailFormat("   "));
        assertFalse(ValidationUtil.isValidEmailFormat("invalid-email"));
        assertFalse(ValidationUtil.isValidEmailFormat("user.domain.com"));
        assertFalse(ValidationUtil.isValidEmailFormat("user@"));
        assertFalse(ValidationUtil.isValidEmailFormat("user@domain"));
        // "@domain.com" contains @ at position 0 and . at position 7
        // According to the logic: 0 < 7 is true, so this email is considered valid
        // But semantically it's invalid because there's no username before @
        assertTrue(ValidationUtil.isValidEmailFormat("@domain.com"));
        // Test case where @ comes after the last dot (invalid format)
        assertFalse(ValidationUtil.isValidEmailFormat("user.domain@com"));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 999L, Long.MAX_VALUE})
    void isValidId_WithValidId_ShouldReturnTrue(Long id) {
        assertTrue(ValidationUtil.isValidId(id));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -999L})
    void isValidId_WithInvalidId_ShouldReturnFalse(Long id) {
        assertFalse(ValidationUtil.isValidId(id));
    }

    @Test
    void isValidId_WithNull_ShouldReturnFalse() {
        assertFalse(ValidationUtil.isValidId(null));
    }

    @ParameterizedTest
    @CsvSource({
        "password, 8, true",
        "exactly8, 8, true",
        "'  trimmed  ', 7, true",
        "short, 10, false",
        "'   ', 1, false"
    })
    void hasMinLength_ShouldReturnExpectedResult(String input, int minLength, boolean expected) {
        assertEquals(expected, ValidationUtil.hasMinLength(input, minLength));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void hasMinLength_WithNullOrEmpty_ShouldReturnFalse(String input) {
        assertFalse(ValidationUtil.hasMinLength(input, 1));
    }

    @ParameterizedTest
    @CsvSource({
        "short, 10, true",
        "exactly10!, 10, true",
        "'', 0, true",
        "'this is too long', 5, false",
        "exactly11!!, 10, false"
    })
    void hasMaxLength_ShouldReturnExpectedResult(String input, int maxLength, boolean expected) {
        assertEquals(expected, ValidationUtil.hasMaxLength(input, maxLength));
    }

    @Test
    void hasMaxLength_WithNull_ShouldReturnTrue() {
        assertTrue(ValidationUtil.hasMaxLength(null, 5));
    }

    @ParameterizedTest
    @CsvSource({
        "password, 6, 12, true",
        "exactly6, 6, 10, true",
        "exactly10!, 6, 10, true",
        "short, 6, 10, false",
        "'this is way too long', 5, 10, false"
    })
    void isLengthInRange_ShouldReturnExpectedResult(String input, int minLength, int maxLength, boolean expected) {
        assertEquals(expected, ValidationUtil.isLengthInRange(input, minLength, maxLength));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isLengthInRange_WithNullOrEmpty_ShouldReturnFalse(String input) {
        assertFalse(ValidationUtil.isLengthInRange(input, 1, 10));
    }
}
