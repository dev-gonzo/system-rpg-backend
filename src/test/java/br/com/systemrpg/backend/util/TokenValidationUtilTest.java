package br.com.systemrpg.backend.util;

import br.com.systemrpg.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenValidationUtilTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private MessageUtil messageUtil;

    private TokenValidationUtil tokenValidationUtil;

    @BeforeEach
    void setUp() {
        tokenValidationUtil = new TokenValidationUtil(jwtService, messageUtil);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String token = "valid.jwt.token";
        String username = "testuser";
        
        when(jwtService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractUsername(token)).thenReturn(username);
        
        boolean result = tokenValidationUtil.isTokenValid(token, username);
        
        assertTrue(result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(jwtService).isTokenExpired(token);
        verify(jwtService).extractUsername(token);
    }

    @Test
    void isTokenValid_WithNullToken_ShouldReturnFalse() {
        String username = "testuser";
        
        boolean result = tokenValidationUtil.isTokenValid(null, username);
        
        assertFalse(result);
        verifyNoInteractions(jwtService);
    }

    @Test
    void isTokenValid_WithEmptyToken_ShouldReturnFalse() {
        String username = "testuser";
        
        boolean result = tokenValidationUtil.isTokenValid("", username);
        
        assertFalse(result);
        verifyNoInteractions(jwtService);
    }

    @Test
    void isTokenValid_WithNullUsername_ShouldReturnFalse() {
        String token = "valid.jwt.token";
        
        boolean result = tokenValidationUtil.isTokenValid(token, null);
        
        assertFalse(result);
        verifyNoInteractions(jwtService);
    }

    @Test
    void isTokenValid_WithEmptyUsername_ShouldReturnFalse() {
        String token = "valid.jwt.token";
        
        boolean result = tokenValidationUtil.isTokenValid(token, "");
        
        assertFalse(result);
        verifyNoInteractions(jwtService);
    }

    @Test
    void isTokenValid_WithBlacklistedToken_ShouldReturnFalse() {
        String token = "blacklisted.jwt.token";
        String username = "testuser";
        
        when(jwtService.isTokenBlacklisted(token)).thenReturn(true);
        
        boolean result = tokenValidationUtil.isTokenValid(token, username);
        
        assertFalse(result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(jwtService, never()).isTokenExpired(any());
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        String token = "expired.jwt.token";
        String username = "testuser";
        
        when(jwtService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.isTokenExpired(token)).thenReturn(true);
        
        boolean result = tokenValidationUtil.isTokenValid(token, username);
        
        assertFalse(result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(jwtService).isTokenExpired(token);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    void isTokenValid_WithMismatchedUsername_ShouldReturnFalse() {
        String token = "valid.jwt.token";
        String username = "testuser";
        String tokenUsername = "differentuser";
        
        when(jwtService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractUsername(token)).thenReturn(tokenUsername);
        
        boolean result = tokenValidationUtil.isTokenValid(token, username);
        
        assertFalse(result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(jwtService).isTokenExpired(token);
        verify(jwtService).extractUsername(token);
    }

    @Test
    void isTokenValid_WithJwtServiceException_ShouldReturnFalse() {
        String token = "invalid.jwt.token";
        String username = "testuser";
        
        when(jwtService.isTokenBlacklisted(token)).thenThrow(new RuntimeException("JWT parsing error"));
        
        boolean result = tokenValidationUtil.isTokenValid(token, username);
        
        assertFalse(result);
        verify(jwtService).isTokenBlacklisted(token);
    }

    @Test
    void isAccessToken_WithValidAccessToken_ShouldReturnTrue() {
        String token = "access.jwt.token";
        
        when(jwtService.extractTokenType(token)).thenReturn("ACCESS");
        
        boolean result = tokenValidationUtil.isAccessToken(token);
        
        assertTrue(result);
        verify(jwtService).extractTokenType(token);
    }

    @Test
    void isAccessToken_WithRefreshToken_ShouldReturnFalse() {
        String token = "refresh.jwt.token";
        
        when(jwtService.extractTokenType(token)).thenReturn("REFRESH");
        
        boolean result = tokenValidationUtil.isAccessToken(token);
        
        assertFalse(result);
        verify(jwtService).extractTokenType(token);
    }

    @Test
    void isAccessToken_WithException_ShouldReturnFalse() {
        String token = "invalid.jwt.token";
        
        when(jwtService.extractTokenType(token)).thenThrow(new RuntimeException("Token parsing error"));
        
        boolean result = tokenValidationUtil.isAccessToken(token);
        
        assertFalse(result);
        verify(jwtService).extractTokenType(token);
    }

    @Test
    void isRefreshToken_WithValidRefreshToken_ShouldReturnTrue() {
        String token = "refresh.jwt.token";
        
        when(jwtService.extractTokenType(token)).thenReturn("REFRESH");
        
        boolean result = tokenValidationUtil.isRefreshToken(token);
        
        assertTrue(result);
        verify(jwtService).extractTokenType(token);
    }

    @Test
    void isRefreshToken_WithAccessToken_ShouldReturnFalse() {
        String token = "access.jwt.token";
        
        when(jwtService.extractTokenType(token)).thenReturn("ACCESS");
        
        boolean result = tokenValidationUtil.isRefreshToken(token);
        
        assertFalse(result);
        verify(jwtService).extractTokenType(token);
    }

    @Test
    void isRefreshToken_WithException_ShouldReturnFalse() {
        String token = "invalid.jwt.token";
        
        when(jwtService.extractTokenType(token)).thenThrow(new RuntimeException("Token parsing error"));
        
        boolean result = tokenValidationUtil.isRefreshToken(token);
        
        assertFalse(result);
        verify(jwtService).extractTokenType(token);
    }

    @Test
    void getTokenErrorMessage_WithBlacklistedToken_ShouldReturnBlacklistedMessage() {
        String token = "blacklisted.jwt.token";
        String expectedMessage = "Token está na lista negra";
        
        when(jwtService.isTokenBlacklisted(token)).thenReturn(true);
        when(messageUtil.getMessage("service.auth.token.blacklisted")).thenReturn(expectedMessage);
        
        String result = tokenValidationUtil.getTokenErrorMessage(token);
        
        assertEquals(expectedMessage, result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(messageUtil).getMessage("service.auth.token.blacklisted");
    }

    @Test
    void getTokenErrorMessage_WithExpiredToken_ShouldReturnExpiredMessage() {
        String token = "expired.jwt.token";
        String expectedMessage = "Token expirado";
        
        when(jwtService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.isTokenExpired(token)).thenReturn(true);
        when(messageUtil.getMessage("service.auth.token.expired")).thenReturn(expectedMessage);
        
        String result = tokenValidationUtil.getTokenErrorMessage(token);
        
        assertEquals(expectedMessage, result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(jwtService).isTokenExpired(token);
        verify(messageUtil).getMessage("service.auth.token.expired");
    }

    @Test
    void getTokenErrorMessage_WithInvalidSignature_ShouldReturnInvalidSignatureMessage() {
        String token = "invalid.jwt.token";
        String expectedMessage = "Assinatura do token inválida";
        
        when(jwtService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(messageUtil.getMessage("service.auth.token.invalid.signature")).thenReturn(expectedMessage);
        
        String result = tokenValidationUtil.getTokenErrorMessage(token);
        
        assertEquals(expectedMessage, result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(jwtService).isTokenExpired(token);
        verify(messageUtil).getMessage("service.auth.token.invalid.signature");
    }

    @Test
    void getTokenErrorMessage_WithException_ShouldReturnInvalidSignatureMessage() {
        String token = "invalid.jwt.token";
        String expectedMessage = "Assinatura do token inválida";
        
        when(jwtService.isTokenBlacklisted(token)).thenThrow(new RuntimeException("JWT error"));
        when(messageUtil.getMessage("service.auth.token.invalid.signature")).thenReturn(expectedMessage);
        
        String result = tokenValidationUtil.getTokenErrorMessage(token);
        
        assertEquals(expectedMessage, result);
        verify(jwtService).isTokenBlacklisted(token);
        verify(messageUtil).getMessage("service.auth.token.invalid.signature");
    }

    @Test
    void maskToken_WithValidToken_ShouldMaskCorrectly() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        String result = tokenValidationUtil.maskToken(token);
        
        assertTrue(result.startsWith("eyJ"));
        assertTrue(result.contains("..."));
        assertTrue(result.endsWith("w5c"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "abc", "abcd", "abcde"})
    void maskToken_WithShortToken_ShouldReturnMasked(String shortToken) {
        String result = tokenValidationUtil.maskToken(shortToken);
        
        assertEquals("***", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void maskToken_WithNullOrEmpty_ShouldReturnMasked(String token) {
        String result = tokenValidationUtil.maskToken(token);
        
        assertEquals("***", result);
    }

    @Test
    void extractTokenFromAuthHeader_WithValidBearerToken_ShouldExtractToken() {
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        
        String result = tokenValidationUtil.extractTokenFromAuthHeader(authHeader);
        
        assertEquals(expectedToken, result);
    }

    @Test
    void extractTokenFromAuthHeader_WithoutBearerPrefix_ShouldReturnNull() {
        String authHeader = "Basic dXNlcjpwYXNz";
        
        String result = tokenValidationUtil.extractTokenFromAuthHeader(authHeader);
        
        assertNull(result);
    }

    @Test
    void extractTokenFromAuthHeader_WithNullHeader_ShouldReturnNull() {
        String result = tokenValidationUtil.extractTokenFromAuthHeader(null);
        
        assertNull(result);
    }

    @Test
    void extractTokenFromAuthHeader_WithEmptyHeader_ShouldReturnNull() {
        String result = tokenValidationUtil.extractTokenFromAuthHeader("");
        
        assertNull(result);
    }

    @Test
    void extractTokenFromAuthHeader_WithOnlyBearer_ShouldReturnEmptyString() {
        String authHeader = "Bearer ";
        
        String result = tokenValidationUtil.extractTokenFromAuthHeader(authHeader);
        
        assertEquals("", result);
    }
}
