package br.com.systemrpg.backend.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe JwkKey.
 */
class JwkKeyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void constructor_NoArgs_ShouldCreateEmptyObject() {
        // Act
        JwkKey jwkKey = new JwkKey();

        // Assert
        assertNotNull(jwkKey);
        assertNull(jwkKey.getKeyType());
        assertNull(jwkKey.getKeyUse());
        assertNull(jwkKey.getKeyId());
        assertNull(jwkKey.getAlgorithm());
        assertNull(jwkKey.getModulus());
        assertNull(jwkKey.getExponent());
    }

    @Test
    void constructor_AllArgs_ShouldCreateObjectWithAllFields() {
        // Arrange
        String keyType = "RSA";
        String keyUse = "sig";
        String keyId = "test-key-id";
        String algorithm = "RS256";
        String modulus = "test-modulus";
        String exponent = "AQAB";

        // Act
        JwkKey jwkKey = new JwkKey(keyType, keyUse, keyId, algorithm, modulus, exponent);

        // Assert
        assertEquals(keyType, jwkKey.getKeyType());
        assertEquals(keyUse, jwkKey.getKeyUse());
        assertEquals(keyId, jwkKey.getKeyId());
        assertEquals(algorithm, jwkKey.getAlgorithm());
        assertEquals(modulus, jwkKey.getModulus());
        assertEquals(exponent, jwkKey.getExponent());
    }

    @Test
    void builder_ShouldCreateObjectCorrectly() {
        // Arrange
        String keyType = "RSA";
        String keyUse = "sig";
        String keyId = "test-key-id";
        String algorithm = "RS256";
        String modulus = "test-modulus";
        String exponent = "AQAB";

        // Act
        JwkKey jwkKey = JwkKey.builder()
                .keyType(keyType)
                .keyUse(keyUse)
                .keyId(keyId)
                .algorithm(algorithm)
                .modulus(modulus)
                .exponent(exponent)
                .build();

        // Assert
        assertEquals(keyType, jwkKey.getKeyType());
        assertEquals(keyUse, jwkKey.getKeyUse());
        assertEquals(keyId, jwkKey.getKeyId());
        assertEquals(algorithm, jwkKey.getAlgorithm());
        assertEquals(modulus, jwkKey.getModulus());
        assertEquals(exponent, jwkKey.getExponent());
    }

    @Test
    void createRsaSigningKey_ShouldCreateCorrectJwkKey() {
        // Arrange
        String keyId = "rsa-key-1";
        String algorithm = "RS256";
        String modulus = "sample-modulus";
        String exponent = "AQAB";

        // Act
        JwkKey jwkKey = JwkKey.createRsaSigningKey(keyId, algorithm, modulus, exponent);

        // Assert
        assertEquals("RSA", jwkKey.getKeyType());
        assertEquals("sig", jwkKey.getKeyUse());
        assertEquals(keyId, jwkKey.getKeyId());
        assertEquals(algorithm, jwkKey.getAlgorithm());
        assertEquals(modulus, jwkKey.getModulus());
        assertEquals(exponent, jwkKey.getExponent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RS256", "RS384", "RS512"})
    void createRsaSigningKey_WithDifferentAlgorithms_ShouldWork(String algorithm) {
        // Arrange
        String keyId = "key-" + algorithm;
        String modulus = "mod-" + algorithm;
        String exponent = "exp-" + algorithm;
        
        // Act
        JwkKey jwkKey = JwkKey.createRsaSigningKey(keyId, algorithm, modulus, exponent);
        
        // Assert
        assertEquals(algorithm, jwkKey.getAlgorithm());
        assertEquals(keyId, jwkKey.getKeyId());
        assertEquals(modulus, jwkKey.getModulus());
        assertEquals(exponent, jwkKey.getExponent());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        JwkKey jwkKey = new JwkKey();
        String keyType = "RSA";
        String keyUse = "sig";
        String keyId = "test-key";
        String algorithm = "RS256";
        String modulus = "test-modulus";
        String exponent = "AQAB";

        // Act
        jwkKey.setKeyType(keyType);
        jwkKey.setKeyUse(keyUse);
        jwkKey.setKeyId(keyId);
        jwkKey.setAlgorithm(algorithm);
        jwkKey.setModulus(modulus);
        jwkKey.setExponent(exponent);

        // Assert
        assertEquals(keyType, jwkKey.getKeyType());
        assertEquals(keyUse, jwkKey.getKeyUse());
        assertEquals(keyId, jwkKey.getKeyId());
        assertEquals(algorithm, jwkKey.getAlgorithm());
        assertEquals(modulus, jwkKey.getModulus());
        assertEquals(exponent, jwkKey.getExponent());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Arrange
        JwkKey jwkKey1 = JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1");
        JwkKey jwkKey2 = JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1");
        JwkKey jwkKey3 = JwkKey.createRsaSigningKey("key2", "RS256", "mod1", "exp1");

        // Act & Assert
        assertEquals(jwkKey2, jwkKey1);
        assertNotEquals(jwkKey3, jwkKey1);
        assertNotEquals(null, jwkKey1);
        assertNotEquals("string", jwkKey1);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Arrange
        JwkKey jwkKey1 = JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1");
        JwkKey jwkKey2 = JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1");

        // Act & Assert
        assertEquals(jwkKey1.hashCode(), jwkKey2.hashCode());
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Arrange
        JwkKey jwkKey = JwkKey.createRsaSigningKey("test-key", "RS256", "test-modulus", "AQAB");

        // Act
        String toString = jwkKey.toString();

        // Assert
        assertTrue(toString.contains("RSA"));
        assertTrue(toString.contains("sig"));
        assertTrue(toString.contains("test-key"));
        assertTrue(toString.contains("RS256"));
        assertTrue(toString.contains("test-modulus"));
        assertTrue(toString.contains("AQAB"));
    }

    @Test
    void jsonSerialization_ShouldUseCorrectPropertyNames() throws JsonProcessingException {
        // Arrange
        JwkKey jwkKey = JwkKey.createRsaSigningKey("test-key", "RS256", "test-modulus", "AQAB");

        // Act
        String json = objectMapper.writeValueAsString(jwkKey);

        // Assert
        assertTrue(json.contains("\"kty\":\"RSA\""));
        assertTrue(json.contains("\"use\":\"sig\""));
        assertTrue(json.contains("\"kid\":\"test-key\""));
        assertTrue(json.contains("\"alg\":\"RS256\""));
        assertTrue(json.contains("\"n\":\"test-modulus\""));
        assertTrue(json.contains("\"e\":\"AQAB\""));
    }

    @Test
    void jsonDeserialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        String json = "{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"test-key\",\"alg\":\"RS256\",\"n\":\"test-modulus\",\"e\":\"AQAB\"}";

        // Act
        JwkKey jwkKey = objectMapper.readValue(json, JwkKey.class);

        // Assert
        assertEquals("RSA", jwkKey.getKeyType());
        assertEquals("sig", jwkKey.getKeyUse());
        assertEquals("test-key", jwkKey.getKeyId());
        assertEquals("RS256", jwkKey.getAlgorithm());
        assertEquals("test-modulus", jwkKey.getModulus());
        assertEquals("AQAB", jwkKey.getExponent());
    }

    @Test
    void jsonSerialization_WithNullFields_ShouldExcludeNulls() throws JsonProcessingException {
        // Arrange
        JwkKey jwkKey = new JwkKey();
        jwkKey.setKeyType("RSA");
        jwkKey.setKeyId("test-key");
        // Deixar outros campos como null

        // Act
        String json = objectMapper.writeValueAsString(jwkKey);

        // Assert
        assertTrue(json.contains("\"kty\":\"RSA\""));
        assertTrue(json.contains("\"kid\":\"test-key\""));
        assertFalse(json.contains("\"use\":"));
        assertFalse(json.contains("\"alg\":"));
        assertFalse(json.contains("\"n\":"));
        assertFalse(json.contains("\"e\":"));
    }

    @Test
    void createRsaSigningKey_WithNullValues_ShouldHandleNulls() {
        // Act
        JwkKey jwkKey = JwkKey.createRsaSigningKey(null, null, null, null);

        // Assert
        assertEquals("RSA", jwkKey.getKeyType());
        assertEquals("sig", jwkKey.getKeyUse());
        assertNull(jwkKey.getKeyId());
        assertNull(jwkKey.getAlgorithm());
        assertNull(jwkKey.getModulus());
        assertNull(jwkKey.getExponent());
    }
}
