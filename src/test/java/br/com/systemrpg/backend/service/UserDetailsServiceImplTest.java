package br.com.systemrpg.backend.service;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe UserDetailsServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Setup test role
        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("USER");
        testRole.setDescription("User role");
        testRole.setIsActive(true);
        testRole.setCreatedAt(LocalDateTime.now());
        testRole.setUpdatedAt(LocalDateTime.now());

        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(true);
        testUser.setRoles(Set.of(testRole));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("$2a$10$hashedpassword", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        
        // Verify authorities
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("test@example.com"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("$2a$10$hashedpassword", result.getPassword());
        assertTrue(result.isEnabled());
    }

    @Test
    void loadUserByUsername_WithInactiveUser_ShouldReturnDisabledUserDetails() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertFalse(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsernameOrEmail(username))
            .thenReturn(Optional.empty());
        when(messageSource.getMessage(
            eq("service.userdetails.user.not.found"), 
            eq(new Object[]{username}), 
            any(Locale.class)
        )).thenReturn("User not found: " + username);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class, 
            () -> userDetailsService.loadUserByUsername(username)
        );
        
        assertEquals("User not found: " + username, exception.getMessage());
    }

    @Test
    void loadUserByUsername_WithUserHavingMultipleRoles_ShouldReturnAllAuthorities() {
        // Arrange
        Role adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("ADMIN");
        adminRole.setDescription("Admin role");
        adminRole.setIsActive(true);
        
        Role managerRole = new Role();
        managerRole.setId(UUID.randomUUID());
        managerRole.setName("MANAGER");
        managerRole.setDescription("Manager role");
        managerRole.setIsActive(true);
        
        testUser.setRoles(Set.of(testRole, adminRole, managerRole));
        
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getAuthorities().size());
        
        Set<String> authorityNames = result.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toSet());
            
        assertTrue(authorityNames.contains("ROLE_USER"));
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("ROLE_MANAGER"));
    }

    @Test
    void loadUserByUsername_WithUserHavingNoRoles_ShouldReturnEmptyAuthorities() {
        // Arrange
        testUser.setRoles(Set.of());
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_WithUserHavingLowercaseRoleName_ShouldReturnUppercaseAuthority() {
        // Arrange
        testRole.setName("user"); // lowercase role name
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_WithEmptyPasswordHash_ShouldReturnUserDetailsWithEmptyPassword() {
        // Arrange
        testUser.setPasswordHash("");
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("", result.getPassword());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void loadUserByUsername_WithNullPasswordHash_ShouldHandleNullPassword() {
        // Arrange
        testUser.setPasswordHash(null);
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        // Spring Security UserDetails.User.builder() throws IllegalArgumentException when password is null
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDetailsService.loadUserByUsername("testuser")
        );
        
        assertEquals("password cannot be null", exception.getMessage());
    }
}
