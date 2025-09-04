package br.com.systemrpg.backend.dto;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe JwksResponse.
 */
class JwksResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void constructor_NoArgs_ShouldCreateEmptyObject() {
        // Act
        JwksResponse jwksResponse = new JwksResponse();

        // Assert
        assertNotNull(jwksResponse);
        assertNull(jwksResponse.getKeys());
    }

    @Test
    void constructor_AllArgs_ShouldCreateObjectWithKeys() {
        // Arrange
        List<JwkKey> keys = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1"),
            JwkKey.createRsaSigningKey("key2", "RS384", "mod2", "exp2")
        );

        // Act
        JwksResponse jwksResponse = new JwksResponse(keys);

        // Assert
        assertEquals(keys, jwksResponse.getKeys());
        assertEquals(2, jwksResponse.getKeys().size());
    }

    @Test
    void builder_ShouldCreateObjectCorrectly() {
        // Arrange
        List<JwkKey> keys = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1")
        );

        // Act
        JwksResponse jwksResponse = JwksResponse.builder()
                .keys(keys)
                .build();

        // Assert
        assertEquals(keys, jwksResponse.getKeys());
    }

    @Test
    void of_WithListOfKeys_ShouldCreateCorrectResponse() {
        // Arrange
        List<JwkKey> keys = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1"),
            JwkKey.createRsaSigningKey("key2", "RS384", "mod2", "exp2")
        );

        // Act
        JwksResponse jwksResponse = JwksResponse.of(keys);

        // Assert
        assertEquals(keys, jwksResponse.getKeys());
        assertEquals(2, jwksResponse.getKeys().size());
    }

    @Test
    void of_WithSingleKey_ShouldCreateResponseWithSingleKeyList() {
        // Arrange
        JwkKey key = JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1");

        // Act
        JwksResponse jwksResponse = JwksResponse.of(key);

        // Assert
        assertNotNull(jwksResponse.getKeys());
        assertEquals(1, jwksResponse.getKeys().size());
        assertEquals(key, jwksResponse.getKeys().get(0));
    }

    @Test
    void of_WithEmptyList_ShouldCreateResponseWithEmptyList() {
        // Arrange
        List<JwkKey> emptyKeys = Collections.emptyList();

        // Act
        JwksResponse jwksResponse = JwksResponse.of(emptyKeys);

        // Assert
        assertNotNull(jwksResponse.getKeys());
        assertTrue(jwksResponse.getKeys().isEmpty());
    }

    @Test
    void of_WithNullList_ShouldCreateResponseWithNullKeys() {
        // Act
        JwksResponse jwksResponse = JwksResponse.of((List<JwkKey>) null);

        // Assert
        assertNull(jwksResponse.getKeys());
    }

    @Test
    void of_WithNullKey_ShouldCreateResponseWithSingleNullKey() {
        // Act
        JwksResponse jwksResponse = JwksResponse.of((JwkKey) null);

        // Assert
        assertNotNull(jwksResponse.getKeys());
        assertEquals(1, jwksResponse.getKeys().size());
        assertNull(jwksResponse.getKeys().get(0));
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        JwksResponse jwksResponse = new JwksResponse();
        List<JwkKey> keys = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1")
        );

        // Act
        jwksResponse.setKeys(keys);

        // Assert
        assertEquals(keys, jwksResponse.getKeys());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Arrange
        List<JwkKey> keys1 = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1")
        );
        List<JwkKey> keys2 = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1")
        );
        List<JwkKey> keys3 = Arrays.asList(
            JwkKey.createRsaSigningKey("key2", "RS256", "mod1", "exp1")
        );

        JwksResponse response1 = JwksResponse.of(keys1);
        JwksResponse response2 = JwksResponse.of(keys2);
        JwksResponse response3 = JwksResponse.of(keys3);

        // Act & Assert
        assertEquals(response2, response1);
        assertNotEquals(response1, response3);
        assertNotEquals(null, response1);
        assertNotEquals("string", response1);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Arrange
        List<JwkKey> keys1 = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1")
        );
        List<JwkKey> keys2 = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1")
        );

        JwksResponse response1 = JwksResponse.of(keys1);
        JwksResponse response2 = JwksResponse.of(keys2);

        // Act & Assert
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void toString_ShouldContainKeys() {
        // Arrange
        JwkKey key = JwkKey.createRsaSigningKey("test-key", "RS256", "test-modulus", "AQAB");
        JwksResponse jwksResponse = JwksResponse.of(key);

        // Act
        String toString = jwksResponse.toString();

        // Assert
        assertTrue(toString.contains("keys"));
        assertTrue(toString.contains("test-key"));
    }

    @Test
    void jsonSerialization_ShouldUseCorrectPropertyNames() throws JsonProcessingException {
        // Arrange
        JwkKey key = JwkKey.createRsaSigningKey("test-key", "RS256", "test-modulus", "AQAB");
        JwksResponse jwksResponse = JwksResponse.of(key);

        // Act
        String json = objectMapper.writeValueAsString(jwksResponse);

        // Assert
        assertTrue(json.contains("\"keys\":"));
        assertTrue(json.contains("\"kty\":\"RSA\""));
        assertTrue(json.contains("\"kid\":\"test-key\""));
    }

    @Test
    void jsonDeserialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        String json = "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"test-key\",\"alg\":\"RS256\",\"n\":\"test-modulus\",\"e\":\"AQAB\"}]}";

        // Act
        JwksResponse jwksResponse = objectMapper.readValue(json, JwksResponse.class);

        // Assert
        assertNotNull(jwksResponse.getKeys());
        assertEquals(1, jwksResponse.getKeys().size());
        
        JwkKey key = jwksResponse.getKeys().get(0);
        assertEquals("RSA", key.getKeyType());
        assertEquals("sig", key.getKeyUse());
        assertEquals("test-key", key.getKeyId());
        assertEquals("RS256", key.getAlgorithm());
        assertEquals("test-modulus", key.getModulus());
        assertEquals("AQAB", key.getExponent());
    }

    @Test
    void jsonSerialization_WithNullKeys_ShouldExcludeNulls() throws JsonProcessingException {
        // Arrange
        JwksResponse jwksResponse = new JwksResponse();
        // keys permanece null

        // Act
        String json = objectMapper.writeValueAsString(jwksResponse);

        // Assert
        assertEquals("{}", json); // Deve ser um objeto vazio devido ao @JsonInclude(JsonInclude.Include.NON_NULL)
    }

    @Test
    void jsonSerialization_WithEmptyKeys_ShouldIncludeEmptyArray() throws JsonProcessingException {
        // Arrange
        JwksResponse jwksResponse = JwksResponse.of(Collections.emptyList());

        // Act
        String json = objectMapper.writeValueAsString(jwksResponse);

        // Assert
        assertTrue(json.contains("\"keys\":[]"));
    }

    @Test
    void jsonSerialization_WithMultipleKeys_ShouldSerializeAll() throws JsonProcessingException {
        // Arrange
        List<JwkKey> keys = Arrays.asList(
            JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1"),
            JwkKey.createRsaSigningKey("key2", "RS384", "mod2", "exp2")
        );
        JwksResponse jwksResponse = JwksResponse.of(keys);

        // Act
        String json = objectMapper.writeValueAsString(jwksResponse);

        // Assert
        assertTrue(json.contains("\"kid\":\"key1\""));
        assertTrue(json.contains("\"kid\":\"key2\""));
        assertTrue(json.contains("\"alg\":\"RS256\""));
        assertTrue(json.contains("\"alg\":\"RS384\""));
    }

    @Test
    void staticFactoryMethods_ShouldReturnDifferentInstances() {
        // Arrange
        JwkKey key = JwkKey.createRsaSigningKey("key1", "RS256", "mod1", "exp1");
        List<JwkKey> keys = Arrays.asList(key);

        // Act
        JwksResponse response1 = JwksResponse.of(keys);
        JwksResponse response2 = JwksResponse.of(key);

        // Assert
        assertNotSame(response1, response2);
        assertEquals(response2.getKeys(), response1.getKeys());
    }
}
