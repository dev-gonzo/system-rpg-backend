package br.com.systemrpg.backend.domain.enumeration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a enumeração RoleEnum.
 */
class RoleEnumTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void values_ShouldReturnAllEnumValues() {
        // Act
        RoleEnum[] values = RoleEnum.values();

        // Assert
        assertEquals(2, values.length);
        assertArrayEquals(new RoleEnum[]{RoleEnum.ADMINISTRADOR, RoleEnum.USUARIO}, values);
    }

    @Test
    void valueOf_WithValidName_ShouldReturnCorrectEnum() {
        // Act & Assert
        assertEquals(RoleEnum.ADMINISTRADOR, RoleEnum.valueOf("ADMINISTRADOR"));
        assertEquals(RoleEnum.USUARIO, RoleEnum.valueOf("USUARIO"));
    }

    @Test
    void valueOf_WithInvalidName_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> RoleEnum.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> RoleEnum.valueOf("admin"));
        assertThrows(IllegalArgumentException.class, () -> RoleEnum.valueOf("user"));
    }

    @Test
    void valueOf_WithNullName_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> RoleEnum.valueOf(null));
    }

    @Test
    void administrador_ShouldHaveCorrectName() {
        // Act
        RoleEnum role = RoleEnum.ADMINISTRADOR;

        // Assert
        assertEquals("admin", role.getName());
        assertEquals("ADMINISTRADOR", role.name());
        assertEquals(0, role.ordinal());
    }

    @Test
    void usuario_ShouldHaveCorrectName() {
        // Act
        RoleEnum role = RoleEnum.USUARIO;

        // Assert
        assertEquals("user", role.getName());
        assertEquals("USUARIO", role.name());
        assertEquals(1, role.ordinal());
    }

    @ParameterizedTest
    @EnumSource(RoleEnum.class)
    void getName_ShouldReturnNonNullValue(RoleEnum role) {
        // Act & Assert
        assertNotNull(role.getName());
        assertFalse(role.getName().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(RoleEnum.class)
    void name_ShouldReturnNonNullValue(RoleEnum role) {
        // Act & Assert
        assertNotNull(role.name());
        assertFalse(role.name().isEmpty());
    }

    @Test
    void ordinal_ShouldReturnCorrectValues() {
        // Act & Assert
        assertEquals(0, RoleEnum.ADMINISTRADOR.ordinal());
        assertEquals(1, RoleEnum.USUARIO.ordinal());
    }

    @Test
    void toString_ShouldReturnEnumName() {
        // Act & Assert
        assertEquals("ADMINISTRADOR", RoleEnum.ADMINISTRADOR.toString());
        assertEquals("USUARIO", RoleEnum.USUARIO.toString());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Arrange
        RoleEnum role1 = RoleEnum.ADMINISTRADOR;
        RoleEnum role2 = RoleEnum.ADMINISTRADOR;
        RoleEnum role3 = RoleEnum.USUARIO;

        // Act & Assert
        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
        assertNotEquals(null, role1);
        assertNotEquals("ADMINISTRADOR", role1);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Arrange
        RoleEnum role1 = RoleEnum.ADMINISTRADOR;
        RoleEnum role2 = RoleEnum.ADMINISTRADOR;

        // Act & Assert
        assertEquals(role2.hashCode(), role1.hashCode());
    }

    @Test
    void compareTo_ShouldWorkCorrectly() {
        // Act & Assert
        assertTrue(RoleEnum.ADMINISTRADOR.compareTo(RoleEnum.USUARIO) < 0);
        assertTrue(RoleEnum.USUARIO.compareTo(RoleEnum.ADMINISTRADOR) > 0);
        assertEquals(0, RoleEnum.ADMINISTRADOR.compareTo(RoleEnum.ADMINISTRADOR));
    }

    @Test
    void jsonSerialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Act
        String adminJson = objectMapper.writeValueAsString(RoleEnum.ADMINISTRADOR);
        String userJson = objectMapper.writeValueAsString(RoleEnum.USUARIO);

        // Assert
        assertEquals("\"ADMINISTRADOR\"", adminJson);
        assertEquals("\"USUARIO\"", userJson);
    }

    @Test
    void jsonDeserialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Act
        RoleEnum adminRole = objectMapper.readValue("\"ADMINISTRADOR\"", RoleEnum.class);
        RoleEnum userRole = objectMapper.readValue("\"USUARIO\"", RoleEnum.class);

        // Assert
        assertEquals(RoleEnum.ADMINISTRADOR, adminRole);
        assertEquals(RoleEnum.USUARIO, userRole);
    }

    @Test
    void jsonDeserialization_WithInvalidValue_ShouldThrowException() {
        // Act & Assert
        assertThrows(JsonProcessingException.class, () -> {
            objectMapper.readValue("\"INVALID\"", RoleEnum.class);
        });
    }

    @Test
    void getDeclaringClass_ShouldReturnCorrectClass() {
        // Act & Assert
        assertEquals(RoleEnum.class, RoleEnum.ADMINISTRADOR.getDeclaringClass());
        assertEquals(RoleEnum.class, RoleEnum.USUARIO.getDeclaringClass());
    }

    @Test
    void enumConstantDirectory_ShouldContainAllValues() {
        // Act
        var constants = RoleEnum.class.getEnumConstants();

        // Assert
        assertEquals(2, constants.length);
        assertTrue(java.util.Arrays.asList(constants).contains(RoleEnum.ADMINISTRADOR));
        assertTrue(java.util.Arrays.asList(constants).contains(RoleEnum.USUARIO));
    }

    @Test
    void switchStatement_ShouldWorkCorrectly() {
        // Act & Assert
        String adminResult = switch (RoleEnum.ADMINISTRADOR) {
            case ADMINISTRADOR -> "Administrator Role";
            case USUARIO -> "User Role";
        };
        
        String userResult = switch (RoleEnum.USUARIO) {
            case ADMINISTRADOR -> "Administrator Role";
            case USUARIO -> "User Role";
        };

        assertEquals("Administrator Role", adminResult);
        assertEquals("User Role", userResult);
    }

    @Test
    void enumInCollection_ShouldWorkCorrectly() {
        // Arrange
        java.util.Set<RoleEnum> roles = java.util.Set.of(RoleEnum.ADMINISTRADOR, RoleEnum.USUARIO);

        // Act & Assert
        assertTrue(roles.contains(RoleEnum.ADMINISTRADOR));
        assertTrue(roles.contains(RoleEnum.USUARIO));
        assertEquals(2, roles.size());
    }

    @Test
    void enumInMap_ShouldWorkCorrectly() {
        // Arrange
        java.util.Map<RoleEnum, String> roleDescriptions = java.util.Map.of(
            RoleEnum.ADMINISTRADOR, "System Administrator",
            RoleEnum.USUARIO, "Regular User"
        );

        // Act & Assert
        assertEquals("System Administrator", roleDescriptions.get(RoleEnum.ADMINISTRADOR));
        assertEquals("Regular User", roleDescriptions.get(RoleEnum.USUARIO));
    }

    @Test
    void getName_ShouldReturnCorrectStringValues() {
        // Act & Assert
        assertEquals("admin", RoleEnum.ADMINISTRADOR.getName());
        assertEquals("user", RoleEnum.USUARIO.getName());
    }

    @Test
    void getName_ShouldBeDifferentFromEnumName() {
        // Act & Assert
        assertNotEquals(RoleEnum.ADMINISTRADOR.name(), RoleEnum.ADMINISTRADOR.getName());
        assertNotEquals(RoleEnum.USUARIO.name(), RoleEnum.USUARIO.getName());
    }

    @Test
    void enumConstantsAreImmutable() {
        // Act
        RoleEnum role1 = RoleEnum.ADMINISTRADOR;
        RoleEnum role2 = RoleEnum.ADMINISTRADOR;

        // Assert
        assertSame(role1, role2); // Enum constants are singletons
    }

    @Test
    void enumImplementsComparable() {
        // Act & Assert
        assertTrue(RoleEnum.ADMINISTRADOR instanceof Comparable);
        assertTrue(RoleEnum.USUARIO instanceof Comparable);
    }

    @Test
    void enumImplementsSerializable() {
        // Act & Assert
        assertTrue(RoleEnum.ADMINISTRADOR instanceof java.io.Serializable);
        assertTrue(RoleEnum.USUARIO instanceof java.io.Serializable);
    }
}
