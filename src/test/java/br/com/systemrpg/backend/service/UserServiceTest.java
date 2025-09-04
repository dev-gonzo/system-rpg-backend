package br.com.systemrpg.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.exception.RecordNotFoundException;
import br.com.systemrpg.backend.repository.RoleRepository;
import br.com.systemrpg.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;


    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("USER");
        testRole.setIsActive(true);

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setRoles(new HashSet<>(Set.of(testRole)));
        testUser.setCreatedAt(LocalDateTime.now());




    }

    @Test
    void findById_WithExistingUser_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findById(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findById_WithNonExistentUser_ShouldThrowRecordNotFoundException() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("service.user.not.found"), any(), any()))
            .thenReturn("Usuário não encontrado");

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> {
            userService.findById(testUserId);
        });
    }

    @Test
    void findAll_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable))
            .thenReturn(userPage);

        // Act
        Page<User> result = userService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser, result.getContent().get(0));
    }

    @Test
    void createUser_WithValidData_ShouldReturnCreatedUser() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        
        Set<String> roleNames = Set.of("USER");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByNameInAndIsActiveTrue(Set.of("USER")))
            .thenReturn(Set.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(newUser, roleNames);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowAlreadyExistsException() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        
        Set<String> roleNames = Set.of("USER");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(true);
        when(messageSource.getMessage(eq("service.user.username.exists"), any(), any()))
            .thenReturn("Username já existe");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(newUser, roleNames);
        });
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowIllegalArgumentException() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("existing@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        
        Set<String> roleNames = Set.of("USER");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(messageSource.getMessage(eq("service.user.email.exists"), any(), any()))
            .thenReturn("Email já existe");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(newUser, roleNames);
        });
    }

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() {
        // Arrange
        User updateUser = new User();
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        Set<String> roleNames = new HashSet<>(Set.of("USER"));
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot(updateUser.getUsername(), testUserId))
            .thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("updated@example.com", testUserId))
            .thenReturn(false);
        when(roleRepository.findByNameInAndIsActiveTrue(roleNames))
            .thenReturn(new HashSet<>(Set.of(testRole)));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(testUserId, updateUser, roleNames);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithExistingEmail_ShouldThrowIllegalArgumentException() {
        // Arrange
        User updateUser = new User();
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        Set<String> roleNames = new HashSet<>(Set.of("USER"));
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("updated@example.com", testUserId))
            .thenReturn(true);
        when(messageSource.getMessage(eq("service.user.email.exists"), any(), any()))
            .thenReturn("Email já existe");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(testUserId, updateUser, roleNames);
        });
    }

    @Test
    void toggleUserStatus_ShouldToggleIsActiveFlag() {
        // Arrange
        Boolean newStatus = false;
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.toggleUserStatus(testUserId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, testUser.getIsActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void deactivateUser_ShouldSetDeletedAt() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deactivateUser(testUserId);

        // Assert
        assertNotNull(testUser.getDeletedAt());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_ShouldDeleteUserPermanently() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(testUserId);

        // Assert
        verify(userRepository).delete(testUser);
    }

    @Test
    void isUsernameAvailable_WithAvailableUsername_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername("availableuser")).thenReturn(false);

        // Act
        boolean result = userService.isUsernameAvailable("availableuser");

        // Assert
        assertTrue(result);
    }

    @Test
    void isUsernameAvailable_WithTakenUsername_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        // Act
        boolean result = userService.isUsernameAvailable("takenuser");

        // Assert
        assertFalse(result);
    }

    @Test
    void isEmailAvailable_WithAvailableEmail_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail("available@example.com")).thenReturn(false);

        // Act
        boolean result = userService.isEmailAvailable("available@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void isEmailAvailable_WithTakenEmail_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // Act
        boolean result = userService.isEmailAvailable("taken@example.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyEmail_WithValidUserId_ShouldVerifyUser() {
        // Arrange
        testUser.setIsEmailVerified(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.verifyEmail(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(testUser.getIsEmailVerified());
        verify(userRepository).save(testUser);
    }

    @Test
    void findByName_WithExistingUser_ShouldReturnPageOfUsers() {
        // Arrange
        String searchTerm = "testuser";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findByUsernameOrFirstNameOrLastNameOrEmailContainingIgnoreCase(searchTerm, pageable))
            .thenReturn(userPage);

        // Act
        Page<User> result = userService.findByName(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser, result.getContent().get(0));
    }

    @Test
    void findByName_WithNonExistentUser_ShouldReturnEmptyPage() {
        // Arrange
        String searchTerm = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findByUsernameOrFirstNameOrLastNameOrEmailContainingIgnoreCase(searchTerm, pageable))
            .thenReturn(emptyPage);

        // Act
        Page<User> result = userService.findByName(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findActiveUsers_ShouldReturnPageOfActiveUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> activeUsersPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(pageable))
            .thenReturn(activeUsersPage);

        // Act
        Page<User> result = userService.findActiveUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser, result.getContent().get(0));
    }

    @Test
    void createUser_WithPasswordHash_ShouldEncodePassword() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setPasswordHash("plainpassword");
        
        Set<String> roleNames = Set.of("USER");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainpassword")).thenReturn("encodedpassword");
        when(roleRepository.findByNameInAndIsActiveTrue(roleNames))
            .thenReturn(Set.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(newUser, roleNames);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder).encode("plainpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithNullRoles_ShouldUseDefaultUserRole() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER"))
            .thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(newUser, null);

        // Assert
        assertNotNull(result);
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithEmptyRoles_ShouldUseDefaultUserRole() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER"))
            .thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(newUser, Set.of());

        // Assert
        assertNotNull(result);
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithInvalidRoles_ShouldThrowIllegalArgumentException() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        
        Set<String> roleNames = Set.of("USER", "ADMIN");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByNameInAndIsActiveTrue(roleNames))
            .thenReturn(Set.of(testRole)); // Only returns USER role, not ADMIN
        when(messageSource.getMessage(eq("service.user.roles.not.found"), any(), any()))
            .thenReturn("Algumas roles não foram encontradas");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(newUser, roleNames);
        });
    }

    @Test
    void updateUser_WithExistingUsername_ShouldThrowIllegalArgumentException() {
        // Arrange
        User updateUser = new User();
        updateUser.setUsername("existinguser");
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        Set<String> roleNames = new HashSet<>(Set.of("USER"));
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot("existinguser", testUserId))
            .thenReturn(true);
        when(messageSource.getMessage(eq("service.user.username.exists"), any(), any()))
            .thenReturn("Username já existe");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(testUserId, updateUser, roleNames);
        });
    }

    @Test
    void updateUser_WithPassword_ShouldEncodePassword() {
        // Arrange
        User updateUser = new User();
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        updateUser.setPasswordHash("newpassword123");
        Set<String> roleNames = new HashSet<>(Set.of("USER"));
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot(updateUser.getUsername(), testUserId))
            .thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("updated@example.com", testUserId))
            .thenReturn(false);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encodednewpassword");
        when(roleRepository.findByNameInAndIsActiveTrue(roleNames))
            .thenReturn(new HashSet<>(Set.of(testRole)));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(testUserId, updateUser, roleNames);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithInvalidRoles_ShouldThrowIllegalArgumentException() {
        // Arrange
        User updateUser = new User();
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        Set<String> roleNames = new HashSet<>(Set.of("USER", "ADMIN"));
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot(updateUser.getUsername(), testUserId))
            .thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("updated@example.com", testUserId))
            .thenReturn(false);
        when(roleRepository.findByNameInAndIsActiveTrue(roleNames))
            .thenReturn(new HashSet<>(Set.of(testRole))); // Only returns USER role, not ADMIN
        when(messageSource.getMessage(eq("service.user.roles.not.found"), any(), any()))
            .thenReturn("Algumas roles não foram encontradas");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(testUserId, updateUser, roleNames);
        });
    }

    @Test
    void createUser_WithDefaultRoleNotFound_ShouldThrowIllegalStateException() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("password123");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedpassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("service.user.role.default.not.found"), any(), any()))
            .thenReturn("Role padrão USER não encontrada");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            userService.createUser(newUser, null);
        });
    }

    @Test
    void updateUser_WithSameUsernameAndEmail_ShouldUpdateSuccessfully() {
        // Arrange
        User updateUser = new User();
        updateUser.setUsername(testUser.getUsername()); // Same username
        updateUser.setEmail(testUser.getEmail()); // Same email
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        Set<String> roleNames = new HashSet<>(Set.of("USER"));
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByNameInAndIsActiveTrue(roleNames))
            .thenReturn(new HashSet<>(Set.of(testRole)));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(testUserId, updateUser, roleNames);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithNullRoles_ShouldClearRoles() {
        // Arrange
        User updateUser = new User();
        updateUser.setUsername("updateduser");
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot("updateduser", testUserId))
            .thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("updated@example.com", testUserId))
            .thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(testUserId, updateUser, null);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithEmptyRoles_ShouldClearRoles() {
        // Arrange
        User updateUser = new User();
        updateUser.setUsername("updateduser");
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        Set<String> emptyRoleNames = new HashSet<>();
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot("updateduser", testUserId))
            .thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("updated@example.com", testUserId))
            .thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(testUserId, updateUser, emptyRoleNames);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_WithNullUserRoles_ShouldInitializeRolesSet() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("password123");
        newUser.setRoles(null); // Explicitly set roles to null
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedpassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.createUser(newUser, null);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(newUser);
    }
}
