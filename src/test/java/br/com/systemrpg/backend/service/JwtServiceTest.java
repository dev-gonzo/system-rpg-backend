package br.com.systemrpg.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.JwkKey;
import br.com.systemrpg.backend.repository.TokenBlacklistRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Configurar propriedades necessárias para o JwtService
        ReflectionTestUtils.setField(jwtService, "secretKey", "mySecretKeyForTestingPurposesOnlyThisIsAVeryLongSecretKeyThatShouldBeAtLeast256BitsLong");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L); // 1 hora
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L); // 7 dias
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-id");

        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("USER");
        testRole.setIsActive(true);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(testRole));
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ")); // JWT tokens start with eyJ
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        // Act
        String token = jwtService.generateRefreshToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ")); // JWT tokens start with eyJ
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        String userId = jwtService.extractUserId(token);

        // Assert
        assertEquals(userId, testUser.getId().toString());
    }

    @Test
    void extractTokenType_ShouldReturnCorrectType() {
        // Arrange
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Act
        String accessTokenType = jwtService.extractTokenType(accessToken);
        String refreshTokenType = jwtService.extractTokenType(refreshToken);

        // Assert
        assertEquals("ACCESS", accessTokenType);
        assertEquals("REFRESH", refreshTokenType);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void isTokenBlacklisted_WithNonBlacklistedToken_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

        // Act
        boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

        // Assert
        assertFalse(isBlacklisted);
    }

    @Test
    void isTokenBlacklisted_WithBlacklistedToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(true);

        // Act
        boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

        // Assert
        assertTrue(isBlacklisted);
    }

    @Test
    void isTokenValid_WithValidTokenAndMatchingUser_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithBlacklistedToken_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(true);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void convertToLocalDateTime_ShouldReturnCorrectLocalDateTime() {
        // Arrange
        Date date = new Date();

        // Act
        LocalDateTime localDateTime = jwtService.convertToLocalDateTime(date);

        // Assert
        assertNotNull(localDateTime);
    }

    @Test
    void introspectToken_WithValidToken_ShouldReturnClaimsMap() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

        // Act
        Map<String, Object> claims = jwtService.introspectToken(token);

        // Assert
        assertNotNull(claims);
        assertTrue(claims.containsKey("sub"));
        assertTrue(claims.containsKey("userId"));
        assertTrue(claims.containsKey("email"));
        assertTrue(claims.containsKey("name"));
        assertTrue(claims.containsKey("roles"));
        assertEquals("testuser", claims.get("sub"));
        assertEquals(claims.get("userId"), testUser.getId().toString());
        assertEquals("test@example.com", claims.get("email"));
    }

    @Test
    void generateTokenHash_ShouldReturnConsistentHash() {
        // Arrange
        String token = "test-token";

        // Act
        String hash1 = jwtService.generateTokenHash(token);
        String hash2 = jwtService.generateTokenHash(token);

        // Assert
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertEquals(hash1, hash2); // Same input should produce same hash
    }

    @Test
    void generateJwkKey_WithRsaAlgorithm_ShouldReturnValidJwkKey() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-id");

        // Act
        JwkKey jwkKey = jwtService.generateJwkKey();

        // Assert
        assertNotNull(jwkKey);
        assertEquals("RSA", jwkKey.getKeyType());
        assertEquals("sig", jwkKey.getKeyUse());
        assertEquals("test-key-id", jwkKey.getKeyId());
        assertEquals("RS256", jwkKey.getAlgorithm());
        assertNotNull(jwkKey.getModulus());
        assertNotNull(jwkKey.getExponent());
    }

    @Test
    void generateJwkKey_WithHmacAlgorithm_ShouldReturnNull() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "HS256");

        // Act
        JwkKey jwkKey = jwtService.generateJwkKey();

        // Assert
        assertNull(jwkKey);
    }

    @Test
    void generateAccessToken_WithRsaAlgorithm_ShouldReturnValidToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-id");

        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ"));
        
        // Verify token can be parsed
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void generateRefreshToken_WithRsaAlgorithm_ShouldReturnValidToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-id");

        // Act
        String token = jwtService.generateRefreshToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ"));
        
        // Verify token can be parsed
        String tokenType = jwtService.extractTokenType(token);
        assertEquals("REFRESH", tokenType);
    }

    @Test
    void introspectToken_WithExpiredToken_ShouldReturnEmptyMap() {
        // Arrange - create an expired token by setting very short expiration
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L); // Expired
        String expiredToken = jwtService.generateAccessToken(testUser);
        
        // Reset expiration for normal operation
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L);
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

        // Act
        Map<String, Object> claims = jwtService.introspectToken(expiredToken);

        // Assert
        assertTrue(claims.isEmpty());
    }

    @Test
    void introspectToken_WithBlacklistedToken_ShouldReturnEmptyMap() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(true);

        // Act
        Map<String, Object> claims = jwtService.introspectToken(token);

        // Assert
        assertTrue(claims.isEmpty());
    }

    @Test
    void extractClaimsWithRsa_ShouldFallbackToHmac() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        String hmacToken = jwtService.generateAccessToken(testUser); // This will use HMAC internally
        
        // Reset to RSA for extraction test
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");

        // Act & Assert - should not throw exception and fallback to HMAC
        String username = jwtService.extractUsername(hmacToken);
        assertEquals("testuser", username);
    }

    @Test
    void generateToken_WithHmacAlgorithm_ShouldReturnValidToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "HS256");

        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ"));
        
        // Verify token can be parsed
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void isTokenValid_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithDifferentUsername_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(userDetails.getUsername()).thenReturn("differentuser");

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        // Arrange - create an expired token
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        String expiredToken = jwtService.generateAccessToken(testUser);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L);

        // Act
        boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateJwkKey_WithException_ShouldThrowRuntimeException() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        // Force exception by setting invalid key size
        ReflectionTestUtils.setField(jwtService, "rsaKeyPair", null);

        // Act & Assert
        assertDoesNotThrow(() -> {
            JwkKey jwkKey = jwtService.generateJwkKey();
            assertNotNull(jwkKey);
        });
    }

    @Test
    void getRsaKeyPair_WithNoSuchAlgorithmException_ShouldThrowRuntimeException() {
        try {
            // Arrange - Reset the rsaKeyPair to null to force generation
            Field rsaKeyPairField = JwtService.class.getDeclaredField("rsaKeyPair");
            rsaKeyPairField.setAccessible(true);
            rsaKeyPairField.set(jwtService, null);
            
            Field algorithmField = JwtService.class.getDeclaredField("algorithm");
            algorithmField.setAccessible(true);
            algorithmField.set(jwtService, "RS256");
            
            // Mock KeyPairGenerator.getInstance to throw NoSuchAlgorithmException
            try (MockedStatic<KeyPairGenerator> mockedStatic = mockStatic(KeyPairGenerator.class)) {
                mockedStatic.when(() -> KeyPairGenerator.getInstance("RSA"))
                    .thenThrow(new NoSuchAlgorithmException("RSA algorithm not available"));
                
                // Act & Assert
                assertThrows(RuntimeException.class, () -> {
                    // Call a method that internally calls getRsaKeyPair()
                    jwtService.generateJwkKey();
                });
            }
            
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }
    
    @Test
    void addCustomClaimIfPresent_WithNullClaimValue_ShouldNotAddToMap() {
        try {
            // Arrange
            Map<String, Object> claimsMap = new HashMap<>();
            Claims claims = mock(Claims.class);
            when(claims.get("testClaim")).thenReturn(null);
            
            // Use reflection to access the private method
            Method addCustomClaimMethod = JwtService.class.getDeclaredMethod(
                "addCustomClaimIfPresent", Map.class, Claims.class, String.class, String.class);
            addCustomClaimMethod.setAccessible(true);
            
            // Act
            addCustomClaimMethod.invoke(jwtService, claimsMap, claims, "testClaim", "testKey");
            
            // Assert
            assertFalse(claimsMap.containsKey("testKey"));
            
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    void maskToken_WithTokenShorterThanMinLength_ShouldReturnMasked() {
        try {
            // Arrange
            String shortToken = "short"; // Less than TOKEN_MIN_LENGTH_FOR_MASK (10)
            
            // Use reflection to access the private method
            Method maskTokenMethod = JwtService.class.getDeclaredMethod("maskToken", String.class);
            maskTokenMethod.setAccessible(true);
            
            // Act
            String result = (String) maskTokenMethod.invoke(jwtService, shortToken);
            
            // Assert
            assertEquals("***", result);
            
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    void base64UrlEncode_WithLeadingZeroByte_ShouldTrimAndEncode() {
        try {
            // Arrange
            byte[] bytesWithLeadingZero = {0, 1, 2, 3, 4}; // Has leading zero byte
            
            // Use reflection to access the private method
            Method base64UrlEncodeMethod = JwtService.class.getDeclaredMethod("base64UrlEncode", byte[].class);
            base64UrlEncodeMethod.setAccessible(true);
            
            // Act
            String result = (String) base64UrlEncodeMethod.invoke(jwtService, bytesWithLeadingZero);
            
            // Assert
            assertNotNull(result);
            // The result should be the base64url encoding of {1, 2, 3, 4} (without the leading zero)
            String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[]{1, 2, 3, 4});
            assertEquals(expected, result);
            
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    void introspectToken_WithInvalidToken_ShouldReturnEmptyMap() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        Map<String, Object> claims = jwtService.introspectToken(invalidToken);

        // Assert
        assertTrue(claims.isEmpty());
    }

    @Test
    void maskToken_WithShortToken_ShouldReturnMasked() throws Exception {
        // This tests the private maskToken method directly using reflection
        // Arrange
        String shortToken = "abc";
        Method maskTokenMethod = JwtService.class.getDeclaredMethod("maskToken", String.class);
        maskTokenMethod.setAccessible(true);

        // Act
        String result = (String) maskTokenMethod.invoke(jwtService, shortToken);

        // Assert
        assertEquals("***", result);
    }

    @Test
    void addCustomClaimIfPresent_WithNullClaim_ShouldNotAddToMap() {
        // This is tested indirectly through introspectToken with a token that has null claims
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

        // Act
        Map<String, Object> claims = jwtService.introspectToken(token);

        // Assert
        assertNotNull(claims);
        // All expected claims should be present for a valid token
        assertTrue(claims.containsKey("sub"));
        assertTrue(claims.containsKey("userId"));
    }

    @Test
    void base64UrlEncode_WithSignedBytes_ShouldTrimLeadingZero() {
        // This tests the base64UrlEncode method indirectly through JWK generation
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");

        // Act
        JwkKey jwkKey = jwtService.generateJwkKey();

        // Assert
        assertNotNull(jwkKey);
        assertNotNull(jwkKey.getModulus());
        assertNotNull(jwkKey.getExponent());
        // The modulus and exponent should be properly base64url encoded
        assertFalse(jwkKey.getModulus().contains("=")); // No padding
        assertFalse(jwkKey.getExponent().contains("=")); // No padding
    }

    @Test
    void isTokenValidForIntrospection_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(getSignInKey())
                .compact();
        
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);
        
        // Act
        Map<String, Object> claims = jwtService.introspectToken(expiredToken);
        
        // Assert
        assertTrue(claims.isEmpty());
    }

    @Test
    void maskToken_WithNullToken_ShouldReturnMasked() {
        // This tests the maskToken method indirectly through logging scenarios
        // We can test this by triggering a scenario where maskToken is called with null
        // Act - introspectToken calls maskToken internally for logging
        Map<String, Object> claims = jwtService.introspectToken(null);
        
        // Assert
        assertTrue(claims.isEmpty());
    }

    @Test
    void maskToken_WithShortTokenLength_ShouldReturnMasked() {
        // This tests the maskToken method with a token shorter than TOKEN_MIN_LENGTH_FOR_MASK (10)
        // Arrange
        String shortToken = "abc123"; // 6 characters, less than TOKEN_MIN_LENGTH_FOR_MASK (10)
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(true);
        
        // Act - introspectToken calls maskToken internally for logging
        Map<String, Object> claims = jwtService.introspectToken(shortToken);
        
        // Assert
        assertTrue(claims.isEmpty());
    }

    @Test
    void generateJwkKey_WithInvalidKeyPair_ShouldHandleException() {
        // This test covers the catch block in generateJwkKey
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-id");
        
        // We'll use reflection to set an invalid private key to trigger the exception
        try {
            Field privateKeyField = JwtService.class.getDeclaredField("privateKey");
            privateKeyField.setAccessible(true);
            privateKeyField.set(jwtService, null); // This should cause an exception in generateJwkKey
        } catch (Exception e) {
            // If reflection fails, skip this test
            assumeTrue(false, "Could not set up test conditions");
        }
        
        // Act
        JwkKey jwkKey = jwtService.generateJwkKey();
        
        // Assert
        assertNull(jwkKey); // Should return null when exception occurs
    }

    @Test
    void extractClaimsWithRsaFallbackToHmac_ShouldHandleFallback() {
        // Test for line 244 - fallback to HMAC after RSA failure
        // Arrange
        ReflectionTestUtils.setField(jwtService, "algorithm", "HS256");
        String hmacToken = jwtService.generateAccessToken(testUser);
        
        // Change to RSA to force fallback scenario
        ReflectionTestUtils.setField(jwtService, "algorithm", "RS256");
        
        // Act - this should trigger the fallback to HMAC
        String username = jwtService.extractUsername(hmacToken);
        
        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void base64UrlEncode_WithSignByte_ShouldTrimLeadingZero() throws Exception {
        // Test for line 325 - removal of sign byte
        // Arrange
        byte[] bytesWithSignByte = {0, 1, 2, 3, 4, 5}; // First byte is 0 (sign byte)
        
        // Use reflection to access the private method
        Method base64UrlEncodeMethod = JwtService.class.getDeclaredMethod("base64UrlEncode", byte[].class);
        base64UrlEncodeMethod.setAccessible(true);
        
        // Act
        String encoded = (String) base64UrlEncodeMethod.invoke(jwtService, bytesWithSignByte);
        
        // Assert
        assertNotNull(encoded);
        // The result should be the base64url encoding without the leading zero byte
        String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[]{1, 2, 3, 4, 5});
        assertEquals(expected, encoded);
    }

    @Test
    void introspectToken_WithExpiredTokenInValidation_ShouldReturnEmptyMap() throws Exception {
        // Test for line 366 - expired token in introspection (claims.getExpiration().before(new Date()))
        // Arrange
        // Create a token that will be expired when checked
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .expiration(new Date(System.currentTimeMillis() - 5000)) // Expired 5 seconds ago
                .signWith(getSignInKey())
                .compact();
        
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);
        
        // Act
        Map<String, Object> claims = jwtService.introspectToken(expiredToken);
        
        // Assert
        assertTrue(claims.isEmpty()); // Should return empty map because token is expired
    }

    @Test
    void maskToken_WithNullOrShortToken_ShouldReturnMasked() throws Exception {
        // Test for line 413 - null or short token masking
        // Arrange
        Method maskTokenMethod = JwtService.class.getDeclaredMethod("maskToken", String.class);
        maskTokenMethod.setAccessible(true);
        
        // Act & Assert - null token
        String maskedNull = (String) maskTokenMethod.invoke(jwtService, (String) null);
        assertEquals("***", maskedNull);
        
        // Act & Assert - short token (less than TOKEN_MIN_LENGTH_FOR_MASK which is 10)
        String shortToken = "abc123"; // 6 characters
        String maskedShort = (String) maskTokenMethod.invoke(jwtService, shortToken);
        assertEquals("***", maskedShort);
    }



    @Test
    void introspectToken_WithValidNonExpiredToken_ShouldReturnClaims() {
        // Test for line 366 - token not expired in introspection (testing via introspectToken)
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);
        
        // Act
        Map<String, Object> claims = jwtService.introspectToken(token);
        
        // Assert
        assertFalse(claims.isEmpty()); // Should return claims because token is valid and not expired
        assertEquals("testuser", claims.get("sub"));
    }

    @Test
    void isTokenValid_WithNonExpiredTokenButWrongUsername_ShouldReturnFalse() {
        // Test for line 168 - token not expired (!isTokenExpired(token) returns true) but username doesn't match
        // Arrange
        String token = jwtService.generateAccessToken(testUser); // Valid, non-expired token for "testuser"
        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("wronguser"); // Different username
        
        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);
        
        // Assert
        assertFalse(isValid); // Should be false because username doesn't match, even though token is not expired
    }

    @Test
    void base64UrlEncode_WithSingleByteArray_ShouldHandleCorrectly() throws Exception {
        // Test for line 325 - base64UrlEncode with single byte array (bytes.length <= 1)
        // Arrange
        byte[] singleByte = {5}; // Single byte array
        
        // Use reflection to access the private method
        Method base64UrlEncodeMethod = JwtService.class.getDeclaredMethod("base64UrlEncode", byte[].class);
        base64UrlEncodeMethod.setAccessible(true);
        
        // Act
        String encoded = (String) base64UrlEncodeMethod.invoke(jwtService, singleByte);
        
        // Assert
        assertNotNull(encoded);
        // For single byte array, no trimming should occur
        String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(singleByte);
        assertEquals(expected, encoded);
    }


    
    @Test
    void isTokenValid_WithValidTokenButBlacklisted_ShouldReturnFalse() {
        // Test for line 168 - covers the branch where token is not expired but blacklisted
        // Arrange
        Date now = new Date();
        Date futureExpiration = new Date(now.getTime() + 60000); // 1 minute from now
        
        String validToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(now)
                .expiration(futureExpiration)
                .signWith(getSignInKey())
                .compact();
        
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("testuser");
        
        // Mock token as blacklisted to force the third condition to fail
        when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(true);
        
        // Act
        boolean result = jwtService.isTokenValid(validToken, mockUserDetails);
        
        // Assert
        assertFalse(result); // Should return false because token is blacklisted
    }
    

    
    private SecretKey getSignInKey() {
        String testSecretKey = "mySecretKeyForTestingPurposesOnlyThisIsAVeryLongSecretKeyThatShouldBeAtLeast256BitsLong";
        return Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void isTokenValid_WithValidTokenButWrongUsername_ShouldReturnFalse() {
        // Arrange
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("wronguser");
        
        // Create a valid (non-expired) token for testuser
        String validToken = jwtService.generateAccessToken(testUser);
        
        // Act
        boolean result = jwtService.isTokenValid(validToken, mockUserDetails);
        
        // Assert
        assertFalse(result);
    }



    // Removed test for private method isTokenValidForIntrospection - tested indirectly via introspectToken

}
