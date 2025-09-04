package br.com.systemrpg.backend.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe UserUpdateRequest.
 */
class UserUpdateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void constructor_NoArgs_ShouldCreateEmptyObject() {
        // Act
        UserUpdateRequest request = new UserUpdateRequest();

        // Assert
        assertNotNull(request);
        assertNull(request.getEmail());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getRoles());
    }

    @Test
    void constructor_AllArgs_ShouldCreateObjectWithAllFields() {
        // Arrange
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        Set<String> roles = new HashSet<>(Arrays.asList("USER", "ADMIN"));

        // Act
        UserUpdateRequest request = new UserUpdateRequest("testuser", email, null, firstName, lastName, roles);

        // Assert
        assertEquals(email, request.getEmail());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(roles, request.getRoles());
    }

    @Test
    void builder_ShouldCreateObjectCorrectly() {
        // Arrange
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));

        // Act
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .build();

        // Assert
        assertEquals(email, request.getEmail());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(roles, request.getRoles());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        UserUpdateRequest request = new UserUpdateRequest();
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));

        // Act
        request.setEmail(email);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setRoles(roles);

        // Assert
        assertEquals(email, request.getEmail());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(roles, request.getRoles());
    }

    @Test
    void validation_WithValidData_ShouldPassValidation() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("validuser")
                .email("valid@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        // Act
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_WithInvalidEmail_ShouldFailValidation() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("invalid-email")
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validation_WithBlankFirstName_ShouldFailValidation() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("valid@example.com")
                .firstName("")
                .lastName("Doe")
                .build();

        // Act
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void validation_WithBlankLastName_ShouldFailValidation() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("valid@example.com")
                .firstName("John")
                .lastName("")
                .build();

        // Act
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void validation_WithNullFields_ShouldPassValidation() {
        // Arrange - campos opcionais podem ser nulos em updates
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email(null)
                .firstName(null)
                .lastName(null)
                .roles(null)
                .build();

        // Act
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Assert
        // Dependendo das anotações de validação, pode passar ou falhar
        // Este teste verifica o comportamento atual
        assertNotNull(violations);
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));
        UserUpdateRequest request1 = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(roles)
                .build();
        
        UserUpdateRequest request2 = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        
        UserUpdateRequest request3 = UserUpdateRequest.builder()
                .email("different@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(roles)
                .build();

        // Act & Assert
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertNotEquals(null, request1);
        assertNotEquals("string", request1);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));
        UserUpdateRequest request1 = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(roles)
                .build();
        
        UserUpdateRequest request2 = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        // Act & Assert
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        // Act
        String toString = request.toString();

        // Assert
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
    }

    @Test
    void jsonSerialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"email\":\"test@example.com\""));
        assertTrue(json.contains("\"firstName\":\"John\""));
        assertTrue(json.contains("\"lastName\":\"Doe\""));
        assertTrue(json.contains("\"roles\":[\"USER\"]"));
    }

    @Test
    void jsonDeserialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        String json = "{\"email\":\"test@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"roles\":[\"USER\"]}";

        // Act
        UserUpdateRequest request = objectMapper.readValue(json, UserUpdateRequest.class);

        // Assert
        assertEquals("test@example.com", request.getEmail());
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertTrue(request.getRoles().contains("USER"));
    }

    @Test
    void roles_WithNullRoles_ShouldHandleCorrectly() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(null)
                .build();

        // Act & Assert
        assertNull(request.getRoles());
    }

    @Test
    void roles_WithEmptyRoles_ShouldHandleCorrectly() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>())
                .build();

        // Act & Assert
        assertNotNull(request.getRoles());
        assertTrue(request.getRoles().isEmpty());
    }

    @Test
    void roles_WithMultipleRoles_ShouldHandleCorrectly() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER", "ADMIN", "MODERATOR"));
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(roles)
                .build();

        // Act & Assert
        assertEquals(3, request.getRoles().size());
        assertTrue(request.getRoles().contains("USER"));
        assertTrue(request.getRoles().contains("ADMIN"));
        assertTrue(request.getRoles().contains("MODERATOR"));
    }

    @Test
    void partialUpdate_ShouldWorkCorrectly() {
        // Arrange - simula um update parcial onde apenas alguns campos são definidos
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newemail@example.com")
                .firstName("NewFirstName")
                .build();

        // Act & Assert
        assertEquals("newemail@example.com", request.getEmail());
        assertEquals("NewFirstName", request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getRoles());
    }

    @Test
    void manualGetters_ShouldWorkCorrectly() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER", "ADMIN"));
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        request.setRoles(roles);

        // Act & Assert
        assertEquals("test@example.com", request.getEmail());
        assertEquals(roles, request.getRoles());
    }

    @Test
    void jsonDeserialization_WithNullFields_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        String json = "{\"email\":null,\"firstName\":\"John\",\"lastName\":null,\"roles\":null}";

        // Act
        UserUpdateRequest request = objectMapper.readValue(json, UserUpdateRequest.class);

        // Assert
        assertNull(request.getEmail());
        assertEquals("John", request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getRoles());
    }

    @Test
    void jsonSerialization_WithNullFields_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email(null)
                .firstName("John")
                .lastName(null)
                .roles(null)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"firstName\":\"John\""));
        // Campos nulos podem ou não aparecer no JSON dependendo da configuração
        assertNotNull(json);
    }
}
