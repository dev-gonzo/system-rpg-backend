package br.com.systemrpg.backend.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

class AuthorizationUtilTest {

    @SuppressWarnings("unchecked")


    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuthentication_WithAuthenticatedUser_ShouldReturnAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        Authentication result = AuthorizationUtil.getCurrentAuthentication();
        
        assertEquals(authentication, result);
    }

    @Test
    void getCurrentAuthentication_WithoutAuthentication_ShouldReturnNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        Authentication result = AuthorizationUtil.getCurrentAuthentication();
        
        assertNull(result);
    }

    @Test
    void isAuthenticated_WithAuthenticatedUser_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("testuser");
        
        boolean result = AuthorizationUtil.isAuthenticated();
        
        assertTrue(result);
    }

    @Test
    void isAuthenticated_WithAnonymousUser_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        
        boolean result = AuthorizationUtil.isAuthenticated();
        
        assertFalse(result);
    }

    @Test
    void isAuthenticated_WithUnauthenticatedUser_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        
        boolean result = AuthorizationUtil.isAuthenticated();
        
        assertFalse(result);
    }

    @Test
    void isAuthenticated_WithNullAuthentication_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        boolean result = AuthorizationUtil.isAuthenticated();
        
        assertFalse(result);
    }

    @Test
    void getCurrentUserAuthorities_WithAuthenticatedUser_ShouldReturnAuthorities() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        )).when(authentication).getAuthorities();
        
        Collection<String> result = AuthorizationUtil.getCurrentUserAuthorities();
        
        assertEquals(2, result.size());
        assertTrue(result.contains("ROLE_USER"));
        assertTrue(result.contains("ROLE_ADMIN"));
    }

    @Test
    void getCurrentUserAuthorities_WithNullAuthentication_ShouldReturnEmptyList() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        Collection<String> result = AuthorizationUtil.getCurrentUserAuthorities();
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getAuthorities_WithValidAuthentication_ShouldReturnAuthorities() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass", 
                Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER")));
        
        Collection<String> result = AuthorizationUtil.getAuthorities(auth);
        
        assertEquals(1, result.size());
        assertTrue(result.contains("ROLE_MANAGER"));
    }

    @Test
    void getAuthorities_WithNullAuthentication_ShouldReturnEmptyList() {
        Collection<String> result = AuthorizationUtil.getAuthorities(null);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void hasAdminRole_WithAdminRole_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasAdminRole();
        
        assertTrue(result);
    }

    @Test
    void hasAdminRole_WithAdminRoleWithoutPrefix_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasAdminRole();
        
        assertTrue(result);
    }

    @Test
    void hasAdminRole_WithoutAdminRole_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasAdminRole();
        
        assertFalse(result);
    }

    @Test
    void hasAdminRole_WithAuthoritiesCollection_ShouldReturnTrue() {
        Collection<String> authorities = List.of("ROLE_ADMIN", "ROLE_USER");
        
        boolean result = AuthorizationUtil.hasAdminRole(authorities);
        
        assertTrue(result);
    }

    @Test
    void hasManagerRole_WithManagerRole_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasManagerRole();
        
        assertTrue(result);
    }

    @Test
    void hasManagerRole_WithManagerRoleWithoutPrefix_ShouldReturnTrue() {
        Collection<String> authorities = List.of("MANAGER");
        
        boolean result = AuthorizationUtil.hasManagerRole(authorities);
        
        assertTrue(result);
    }

    @Test
    void hasManagerRole_WithoutManagerRole_ShouldReturnFalse() {
        Collection<String> authorities = List.of("ROLE_USER");
        
        boolean result = AuthorizationUtil.hasManagerRole(authorities);
        
        assertFalse(result);
    }

    @Test
    void hasUserRole_WithUserRole_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasUserRole();
        
        assertTrue(result);
    }

    @Test
    void hasUserRole_WithUserRoleWithoutPrefix_ShouldReturnTrue() {
        Collection<String> authorities = List.of("USER");
        
        boolean result = AuthorizationUtil.hasUserRole(authorities);
        
        assertTrue(result);
    }

    @Test
    void hasUserRole_WithoutUserRole_ShouldReturnFalse() {
        Collection<String> authorities = List.of("ROLE_ADMIN");
        
        boolean result = AuthorizationUtil.hasUserRole(authorities);
        
        assertFalse(result);
    }

    @Test
    void hasAnyRole_WithMatchingRole_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_MANAGER")
        )).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasAnyRole("ROLE_ADMIN", "ROLE_MANAGER");
        
        assertTrue(result);
    }

    @Test
    void hasAnyRole_WithoutMatchingRole_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasAnyRole("ROLE_ADMIN", "ROLE_MANAGER");
        
        assertFalse(result);
    }

    @Test
    void hasAnyRole_WithAuthoritiesCollection_ShouldReturnTrue() {
        Collection<String> authorities = List.of("ROLE_USER", "ROLE_MANAGER");
        
        boolean result = AuthorizationUtil.hasAnyRole(authorities, "ROLE_ADMIN", "ROLE_MANAGER");
        
        assertTrue(result);
    }

    @Test
    void hasAnyRole_WithEmptyAuthorities_ShouldReturnFalse() {
        Collection<String> authorities = List.of();
        
        boolean result = AuthorizationUtil.hasAnyRole(authorities, "ROLE_ADMIN");
        
        assertFalse(result);
    }

    @Test
    void hasAnyRole_WithEmptyRoles_ShouldReturnFalse() {
        Collection<String> authorities = List.of("ROLE_USER");
        
        boolean result = AuthorizationUtil.hasAnyRole(authorities);
        
        assertFalse(result);
    }

    @Test
    void hasAllRoles_WithAllMatchingRoles_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        )).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasAllRoles("ROLE_USER", "ROLE_MANAGER");
        
        assertTrue(result);
    }

    @Test
    void hasAllRoles_WithMissingRole_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        boolean result = AuthorizationUtil.hasAllRoles("ROLE_USER", "ROLE_ADMIN");
        
        assertFalse(result);
    }

    @Test
    void hasAllRoles_WithAuthoritiesCollection_ShouldReturnTrue() {
        Collection<String> authorities = List.of("ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN");
        
        boolean result = AuthorizationUtil.hasAllRoles(authorities, "ROLE_USER", "ROLE_MANAGER");
        
        assertTrue(result);
    }

    @Test
    void hasAllRoles_WithEmptyAuthorities_ShouldReturnFalse() {
        Collection<String> authorities = List.of();
        
        boolean result = AuthorizationUtil.hasAllRoles(authorities, "ROLE_USER");
        
        assertFalse(result);
    }

    @Test
    void hasAllRoles_WithEmptyRoles_ShouldReturnFalse() {
        Collection<String> authorities = List.of("ROLE_USER", "ROLE_ADMIN");
        
        boolean result = AuthorizationUtil.hasAllRoles(authorities);
        
        assertFalse(result);
    }

    @Test
    void getCurrentUsername_WithStringPrincipal_ShouldReturnUsername() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("testuser");
        
        String result = AuthorizationUtil.getCurrentUsername();
        
        assertEquals("testuser", result);
    }

    @Test
    void getCurrentUsername_WithObjectPrincipal_ShouldReturnToString() {
        Object principal = new Object() {
            @Override
            public String toString() {
                return "customuser";
            }
        };
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        
        String result = AuthorizationUtil.getCurrentUsername();
        
        assertEquals("customuser", result);
    }

    @Test
    void getCurrentUsername_WithNullAuthentication_ShouldReturnNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        String result = AuthorizationUtil.getCurrentUsername();
        
        assertNull(result);
    }

    @Test
    void isResourceOwner_WithMatchingUsername_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("testuser");
        
        boolean result = AuthorizationUtil.isResourceOwner("testuser");
        
        assertTrue(result);
    }

    @Test
    void isResourceOwner_WithDifferentUsername_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("testuser");
        
        boolean result = AuthorizationUtil.isResourceOwner("otheruser");
        
        assertFalse(result);
    }

    @Test
    void isResourceOwner_WithNullCurrentUsername_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        boolean result = AuthorizationUtil.isResourceOwner("testuser");
        
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({
        "ROLE_ADMIN, adminuser, otheruser, true",
        "ROLE_MANAGER, manageruser, otheruser, true", 
        "ROLE_USER, testuser, testuser, true",
        "ROLE_USER, testuser, otheruser, false"
    })
    void canAccessResource_ShouldReturnExpectedResult(String role, String principal, String resourceOwner, boolean expected) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Arrays.asList(new SimpleGrantedAuthority(role))).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(principal);
        
        boolean result = AuthorizationUtil.canAccessResource(resourceOwner);
        
        assertEquals(expected, result);
    }
}
