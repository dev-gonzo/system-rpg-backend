package br.com.systemrpg.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class TokenUtilsTest {

    @Test
    void extractToken_WithValidBearerToken_ShouldExtractToken() {
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        
        String result = TokenUtils.extractToken(authHeader);
        
        assertEquals(expectedToken, result);
    }

    @Test
    void extractToken_WithBearerTokenAndExtraSpaces_ShouldTrimAndExtractToken() {
        String authHeader = "Bearer   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9   ";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        
        String result = TokenUtils.extractToken(authHeader);
        
        assertEquals(expectedToken, result);
    }

    @ParameterizedTest
    @CsvSource({
        "'Bearer ', '', 'Should return empty string for Bearer with space'",
        "'Bearer', null, 'Should return null for Bearer without space'",
        "null, null, 'Should return null for null auth header'",
        "'', null, 'Should return null for empty auth header'"
    })
    void extractToken_WithInvalidInputs_ShouldReturnExpectedResult(String authHeader, String expected, String description) {
        String result = TokenUtils.extractToken("null".equals(authHeader) ? null : authHeader);
        
        if ("null".equals(expected)) {
            assertNull(result, description);
        } else {
            assertEquals(expected, result, description);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "'Basic dXNlcjpwYXNzd29yZA==', 'Basic auth header should return null'",
        "'bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9', 'Lowercase bearer should return null'",
        "'Token Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9', 'Bearer in middle should return null'"
    })
    void extractToken_WithVariousInvalidInputs_ShouldReturnNull(String authHeader, String description) {
        String result = TokenUtils.extractToken(authHeader);
        assertNull(result, description);
    }

    @Test
    void extractToken_WithWhitespaceOnlyAfterBearer_ShouldReturnEmptyString() {
        String result = TokenUtils.extractToken("Bearer    ");
        assertEquals("", result, "Whitespace only after Bearer should return empty string");
    }

    @Test
    void extractToken_WithComplexJwtToken_ShouldExtractCorrectly() {
        String complexToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String authHeader = "Bearer " + complexToken;
        
        String result = TokenUtils.extractToken(authHeader);
        
        assertEquals(complexToken, result);
    }

    @Test
    void extractToken_WithTokenContainingSpaces_ShouldExtractWithoutTrimming() {
        String tokenWithSpaces = "token with spaces";
        String authHeader = "Bearer " + tokenWithSpaces;
        
        String result = TokenUtils.extractToken(authHeader);
        
        assertEquals("token with spaces", result);
    }

    @Test
    void testConstructor() throws Exception {
        Constructor<TokenUtils> constructor = TokenUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        TokenUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
