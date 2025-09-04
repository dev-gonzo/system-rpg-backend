package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.RoleResponse;
import br.com.systemrpg.backend.dto.UserCreateRequest;
import br.com.systemrpg.backend.dto.UserResponse;
import br.com.systemrpg.backend.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe UserMapper.
 */
class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private Role testRole;
    private UserCreateRequest userCreateRequest;
    private UserUpdateRequest userUpdateRequest;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        
        // Setup test role
        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("ROLE_USER");
        testRole.setDescription("User role");
        
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(true);
        testUser.setRoles(Set.of(testRole));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        // Setup test requests
        userCreateRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();
                
        userUpdateRequest = UserUpdateRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword123")
                .firstName("Updated")
                .lastName("User")
                .build();
    }

    @Test
    void toEntity_FromUserCreateRequest_ShouldMapCorrectly() {
        // Act
        User result = userMapper.toEntity(userCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(result.getUsername(), userCreateRequest.getUsername());
        assertEquals(result.getEmail(), userCreateRequest.getEmail());
        assertEquals(result.getPasswordHash(), userCreateRequest.getPassword());
        assertEquals(result.getFirstName(), userCreateRequest.getFirstName());
        assertEquals(result.getLastName(), userCreateRequest.getLastName());
        
        // Verify ignored fields
        assertNull(result.getId());
        assertTrue(result.getRoles() == null || result.getRoles().isEmpty());
        // These fields have default values in the User entity
        assertTrue(result.getIsActive()); // Default is true
        assertFalse(result.getIsEmailVerified()); // Default is false
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
        assertNull(result.getLastLoginAt());
        assertNull(result.getPasswordChangedAt());
        assertNull(result.getDeletedAt());
    }

    @Test
    void toEntity_FromUserUpdateRequest_ShouldMapCorrectly() {
        // Act
        User result = userMapper.toEntity(userUpdateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(result.getUsername(), userUpdateRequest.getUsername());
        assertEquals(result.getEmail(), userUpdateRequest.getEmail());
        assertEquals(result.getPasswordHash(), userUpdateRequest.getPassword());
        assertEquals(result.getFirstName(), userUpdateRequest.getFirstName());
        assertEquals(result.getLastName(), userUpdateRequest.getLastName());
        
        // Verify ignored fields
        assertNull(result.getId());
        assertTrue(result.getRoles() == null || result.getRoles().isEmpty());
        // These fields have default values in the User entity
        assertTrue(result.getIsActive()); // Default is true
        assertFalse(result.getIsEmailVerified()); // Default is false
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
        assertNull(result.getLastLoginAt());
        assertNull(result.getPasswordChangedAt());
        assertNull(result.getDeletedAt());
    }

    @Test
    void updateEntityFromRequest_ShouldUpdateExistingUser() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");
        existingUser.setPasswordHash("oldpassword");
        existingUser.setFirstName("Old");
        existingUser.setLastName("User");
        existingUser.setIsActive(false);
        existingUser.setRoles(Set.of(testRole));
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
        existingUser.setCreatedAt(originalCreatedAt);

        // Act
        userMapper.updateEntityFromRequest(userUpdateRequest, existingUser);

        // Assert
        assertEquals(existingUser.getUsername(), userUpdateRequest.getUsername());
        assertEquals(existingUser.getEmail(), userUpdateRequest.getEmail());
        assertEquals(existingUser.getPasswordHash(), userUpdateRequest.getPassword());
        assertEquals(existingUser.getFirstName(), userUpdateRequest.getFirstName());
        assertEquals(existingUser.getLastName(), userUpdateRequest.getLastName());
        
        // Verify ignored fields remain unchanged
        assertNotNull(existingUser.getId());
        assertEquals(Set.of(testRole), existingUser.getRoles());
        assertFalse(existingUser.getIsActive());
        assertEquals(originalCreatedAt, existingUser.getCreatedAt());
    }

    @Test
    void toResponse_ShouldMapUserToUserResponse() {
        // Act
        UserResponse result = userMapper.toResponse(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), testUser.getId());
        assertEquals(result.getUsername(), testUser.getUsername());
        assertEquals(result.getEmail(), testUser.getEmail());
        assertEquals(result.getFirstName(), testUser.getFirstName());
        assertEquals(result.getLastName(), testUser.getLastName());
        assertEquals(result.getIsActive(), testUser.getIsActive());
        assertEquals(result.getIsEmailVerified(), testUser.getIsEmailVerified());
        assertEquals(result.getCreatedAt(), testUser.getCreatedAt());
        assertEquals(result.getUpdatedAt(), testUser.getUpdatedAt());
        assertEquals(result.getLastLoginAt(), testUser.getLastLoginAt());
        
        // Verify roles mapping
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        RoleResponse roleResponse = result.getRoles().iterator().next();
        assertEquals(roleResponse.getId(), testRole.getId());
        assertEquals(roleResponse.getName(), testRole.getName());
        assertEquals(roleResponse.getDescription(), testRole.getDescription());
    }

    @Test
    void toRoleResponse_ShouldMapRoleToRoleResponse() {
        // Act
        RoleResponse result = userMapper.toRoleResponse(testRole);

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), testRole.getId());
        assertEquals(result.getName(), testRole.getName());
        assertEquals(result.getDescription(), testRole.getDescription());
    }

    @Test
    void mapRoles_WithNullRoles_ShouldReturnEmptySet() {
        // Act
        Set<RoleResponse> result = userMapper.mapRoles(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapRoles_WithEmptyRoles_ShouldReturnEmptySet() {
        // Act
        Set<RoleResponse> result = userMapper.mapRoles(Set.of());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapRoles_WithRoles_ShouldMapCorrectly() {
        // Arrange
        Role role2 = new Role();
        role2.setId(UUID.randomUUID());
        role2.setName("ROLE_ADMIN");
        role2.setDescription("Admin role");
        
        Set<Role> roles = Set.of(testRole, role2);

        // Act
        Set<RoleResponse> result = userMapper.mapRoles(roles);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify all roles are mapped
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("ROLE_USER")));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")));
    }

    @Test
    void toEntity_WithNullRequest_ShouldReturnNull() {
        // Act
        User result = userMapper.toEntity((UserCreateRequest) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toResponse_WithNullUser_ShouldReturnNull() {
        // Act
        UserResponse result = userMapper.toResponse(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toRoleResponse_WithNullRole_ShouldReturnNull() {
        // Act
        RoleResponse result = userMapper.toRoleResponse(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_WithNullUserUpdateRequest_ShouldReturnNull() {
        // Act
        User result = userMapper.toEntity((UserUpdateRequest) null);

        // Assert
        assertNull(result);
    }

    @Test
    void updateEntityFromRequest_WithNullRequest_ShouldNotUpdateUser() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setUsername("originaluser");
        existingUser.setEmail("original@example.com");
        existingUser.setPasswordHash("originalpassword");
        existingUser.setFirstName("Original");
        existingUser.setLastName("User");
        
        String originalUsername = existingUser.getUsername();
        String originalEmail = existingUser.getEmail();
        String originalPassword = existingUser.getPasswordHash();
        String originalFirstName = existingUser.getFirstName();
        String originalLastName = existingUser.getLastName();

        // Act
        userMapper.updateEntityFromRequest(null, existingUser);

        // Assert - values should remain unchanged
        assertEquals(originalUsername, existingUser.getUsername());
        assertEquals(originalEmail, existingUser.getEmail());
        assertEquals(originalPassword, existingUser.getPasswordHash());
        assertEquals(originalFirstName, existingUser.getFirstName());
        assertEquals(originalLastName, existingUser.getLastName());
    }

    @Test
    void updateEntityFromRequest_WithNullFields_ShouldNotUpdateNullFields() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setUsername("originaluser");
        existingUser.setEmail("original@example.com");
        existingUser.setPasswordHash("originalpassword");
        existingUser.setFirstName("Original");
        existingUser.setLastName("User");
        
        UserUpdateRequest requestWithNulls = UserUpdateRequest.builder()
                .username(null)
                .email(null)
                .password(null)
                .firstName(null)
                .lastName(null)
                .build();

        // Act
        userMapper.updateEntityFromRequest(requestWithNulls, existingUser);

        // Assert - original values should remain unchanged due to null values
        assertEquals("originaluser", existingUser.getUsername());
        assertEquals("original@example.com", existingUser.getEmail());
        assertEquals("originalpassword", existingUser.getPasswordHash());
        assertEquals("Original", existingUser.getFirstName());
        assertEquals("User", existingUser.getLastName());
    }
}
