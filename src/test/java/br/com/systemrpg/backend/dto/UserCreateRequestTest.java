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
 * Testes unitários para a classe UserCreateRequest.
 */
class UserCreateRequestTest {

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
        UserCreateRequest request = new UserCreateRequest();

        // Assert
        assertNotNull(request);
        assertNull(request.getUsername());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getRoles());
    }

    @Test
    void constructor_AllArgs_ShouldCreateObjectWithAllFields() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        Set<String> roles = new HashSet<>(Arrays.asList("USER", "ADMIN"));

        // Act
        UserCreateRequest request = new UserCreateRequest(username, email, password, firstName, lastName, roles);

        // Assert
        assertEquals(username, request.getUsername());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(roles, request.getRoles());
    }

    @Test
    void builder_ShouldCreateObjectCorrectly() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));

        // Act
        UserCreateRequest request = UserCreateRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .build();

        // Assert
        assertEquals(username, request.getUsername());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(roles, request.getRoles());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        String username = "testuser";
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));

        // Act
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setRoles(roles);

        // Assert
        assertEquals(username, request.getUsername());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(roles, request.getRoles());
    }

    @Test
    void validation_WithValidData_ShouldPassValidation() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("ValidPass123!")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        // Act
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_WithBlankUsername_ShouldFailValidation() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("")
                .email("valid@example.com")
                .password("ValidPass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void validation_WithInvalidEmail_ShouldFailValidation() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("validuser")
                .email("invalid-email")
                .password("ValidPass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validation_WithBlankPassword_ShouldFailValidation() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("")
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void validation_WithBlankFirstName_ShouldFailValidation() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("ValidPass123!")
                .firstName("")
                .lastName("Doe")
                .build();

        // Act
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void validation_WithBlankLastName_ShouldFailValidation() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("ValidPass123!")
                .firstName("John")
                .lastName("")
                .build();

        // Act
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));
        UserCreateRequest request1 = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .roles(roles)
                .build();
        
        UserCreateRequest request2 = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        
        UserCreateRequest request3 = UserCreateRequest.builder()
                .username("differentuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .roles(roles)
                .build();

        // Act & Assert
        assertEquals(request2, request1);
        assertNotEquals(request3, request1);
        assertNotEquals(null, request1);
        assertNotEquals("string", request1);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER"));
        UserCreateRequest request1 = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .roles(roles)
                .build();
        
        UserCreateRequest request2 = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
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
        UserCreateRequest request = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        // Act
        String toString = request.toString();

        // Assert
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        // Não deve mostrar a senha no toString por segurança
    }

    @Test
    void jsonSerialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"username\":\"testuser\""));
        assertTrue(json.contains("\"email\":\"test@example.com\""));
        assertTrue(json.contains("\"firstName\":\"John\""));
        assertTrue(json.contains("\"lastName\":\"Doe\""));
        assertTrue(json.contains("\"roles\":[\"USER\"]"));
    }

    @Test
    void jsonDeserialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Arrange
        String json = "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"Password123!\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"roles\":[\"USER\"]}";

        // Act
        UserCreateRequest request = objectMapper.readValue(json, UserCreateRequest.class);

        // Assert
        assertEquals("testuser", request.getUsername());
        assertEquals("test@example.com", request.getEmail());
        assertEquals("Password123!", request.getPassword());
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertTrue(request.getRoles().contains("USER"));
    }

    @Test
    void roles_WithNullRoles_ShouldHandleCorrectly() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
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
        UserCreateRequest request = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .roles(new HashSet<>())
                .build();

        // Act & Assert
        assertNotNull(request.getRoles());
        assertTrue(request.getRoles().isEmpty());
    }

    @Test
    void manualGetters_ShouldWorkCorrectly() {
        // Arrange
        Set<String> roles = new HashSet<>(Arrays.asList("USER", "ADMIN"));
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setRoles(roles);

        // Act & Assert
        assertEquals("testuser", request.getUsername());
        assertEquals(roles, request.getRoles());
    }
}
