package br.com.systemrpg.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.TokenBlacklist;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.AuthResponse;
import br.com.systemrpg.backend.dto.JwkKey;
import br.com.systemrpg.backend.dto.JwksResponse;
import br.com.systemrpg.backend.dto.LoginRequest;
import br.com.systemrpg.backend.dto.RefreshTokenRequest;
import br.com.systemrpg.backend.dto.TokenIntrospectRequest;
import br.com.systemrpg.backend.dto.TokenIntrospectResponse;
import br.com.systemrpg.backend.repository.TokenBlacklistRepository;
import br.com.systemrpg.backend.repository.UserRepository;
import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.TokenValidationUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MessageUtil messageUtil;

    @Mock
    private TokenValidationUtil tokenValidationUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
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

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");
        refreshTokenRequest.setAccessToken("old-access-token");
    }

    @Test
    @SuppressWarnings("unchecked")
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        when(authentication.getAuthorities())
            .thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(jwtService.generateAccessToken(testUser))
            .thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser))
            .thenReturn("refresh-token");
        when(jwtService.extractExpiration("access-token"))
            .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtService.convertToLocalDateTime(any()))
            .thenReturn(LocalDateTime.now().plusHours(1));

        // Act
        AuthResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("testuser", result.getUser().getUsername());
        assertEquals("test@example.com", result.getUser().getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void login_WithInactiveUser_ShouldThrowBadCredentialsException() {
        // Arrange
        testUser.setIsActive(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        when(messageUtil.getMessage("service.auth.user.inactive"))
            .thenReturn("Usuário inativo");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.empty());
        when(messageUtil.getMessage("service.auth.user.not.found"))
            .thenReturn("Usuário não encontrado");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAuthResponse() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String userId = testUser.getId().toString();
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para getUserFromRefreshToken
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Mock para validateUserForRefresh
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(true);
        
        // Mock para generateNewTokens
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtService.extractExpiration("new-access-token"))
            .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtService.convertToLocalDateTime(any()))
            .thenReturn(LocalDateTime.now().plusHours(1));

        // Act
        AuthResponse result = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
    }

    @Test
    void refreshToken_WithBlacklistedToken_ShouldThrowBadCredentialsException() {
        // Arrange
        when(tokenValidationUtil.isTokenValid(eq("valid-refresh-token"), anyString())).thenReturn(false);
        when(tokenValidationUtil.getTokenErrorMessage("valid-refresh-token"))
            .thenReturn("Refresh token foi invalidado");
        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("testuser");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
    void refreshToken_WithInvalidTokenType_ShouldThrowBadCredentialsException() {
        // Arrange
        when(tokenValidationUtil.isTokenValid(eq("valid-refresh-token"), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken("valid-refresh-token")).thenReturn(false);
        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("testuser");
        when(messageUtil.getMessage("service.auth.refresh.token.invalid"))
            .thenReturn("Refresh token inválido");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
    void logout_WithValidToken_ShouldCompleteSuccessfully() {
        // Arrange
        String authHeader = "Bearer valid-access-token";
        String accessToken = "valid-access-token";
        String username = "testuser";
        String userId = testUser.getId().toString();
        
        when(jwtService.extractUsername(accessToken)).thenReturn(username);
        when(tokenValidationUtil.isTokenValid(accessToken, username)).thenReturn(true);
        when(jwtService.extractUserId(accessToken)).thenReturn(userId);
        when(jwtService.generateTokenHash(accessToken)).thenReturn("token-hash");
        when(jwtService.extractExpiration(accessToken)).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtService.convertToLocalDateTime(any())).thenReturn(LocalDateTime.now().plusHours(1));
        
        // Act
        assertDoesNotThrow(() -> authService.logout(authHeader));
        
        // Assert
        verify(tokenBlacklistRepository).save(any());
    }

    @Test
    void logout_WithInvalidToken_ShouldThrowRuntimeException() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        String accessToken = "invalid-token";
        String username = "testuser";
        
        when(jwtService.extractUsername(accessToken)).thenReturn(username);
        when(tokenValidationUtil.isTokenValid(accessToken, username)).thenReturn(false);
        when(tokenValidationUtil.maskToken(accessToken)).thenReturn("***-token");
        when(tokenValidationUtil.getTokenErrorMessage(accessToken)).thenReturn("Token inválido");
        when(messageUtil.getMessage("service.auth.logout.error")).thenReturn("Erro durante logout");
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.logout(authHeader);
        });
    }

    @Test
    void logout_WithException_ShouldThrowRuntimeException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        when(jwtService.extractUsername(anyString())).thenThrow(new RuntimeException("JWT parsing error"));
        when(messageUtil.getMessage("service.auth.logout.error")).thenReturn("Erro durante logout");
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.logout(authHeader);
        });
    }

    @Test
    void introspect_WithValidToken_ShouldReturnActiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken("valid-token");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");
        claims.put("exp", System.currentTimeMillis() / 1000 + 3600);
        
        when(jwtService.introspectToken("valid-token")).thenReturn(claims);
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertTrue(response.getActive());
        assertEquals(claims, response.getClaims());
    }

    @Test
    void introspect_WithBearerPrefix_ShouldStripPrefixAndReturnActiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken("Bearer valid-token");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");
        
        when(jwtService.introspectToken("valid-token")).thenReturn(claims);
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertTrue(response.getActive());
        assertEquals(claims, response.getClaims());
    }

    @Test
    void introspect_WithInvalidToken_ShouldReturnInactiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken("invalid-token");
        
        when(jwtService.introspectToken("invalid-token")).thenReturn(new HashMap<>());
        when(tokenValidationUtil.getTokenErrorMessage("invalid-token")).thenReturn("Token inválido");
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertFalse(response.getActive());
        assertEquals("Token inválido", response.getError());
    }

    @Test
    void introspect_WithEmptyToken_ShouldReturnInactiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken("");
        
        when(messageUtil.getMessage("service.auth.token.empty")).thenReturn("Token vazio");
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertFalse(response.getActive());
        assertEquals("Token vazio", response.getError());
    }

    @Test
    void introspect_WithNullToken_ShouldReturnInactiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken(null);
        
        when(messageUtil.getMessage("service.auth.token.empty")).thenReturn("Token vazio");
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertFalse(response.getActive());
        assertEquals("Token vazio", response.getError());
    }

    @Test
    void introspect_WithException_ShouldReturnInactiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken("valid-token");
        
        when(jwtService.introspectToken("valid-token")).thenThrow(new RuntimeException("JWT error"));
        when(messageUtil.getMessage("service.auth.introspect.error")).thenReturn("Erro na introspecção");
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertFalse(response.getActive());
        assertEquals("Erro na introspecção", response.getError());
    }

    @Test
    void generateJwks_WithValidKey_ShouldReturnJwksResponse() {
        // Arrange
        JwkKey jwkKey = JwkKey.builder()
            .keyType("RSA")
            .keyUse("sig")
            .keyId("test-key-id")
            .algorithm("RS256")
            .modulus("test-modulus")
            .exponent("AQAB")
            .build();
        
        when(jwtService.generateJwkKey()).thenReturn(jwkKey);
        
        // Act
        JwksResponse response = authService.generateJwks();
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getKeys().size());
        assertEquals(jwkKey, response.getKeys().get(0));
    }

    @Test
    void generateJwks_WithNullKey_ShouldReturnEmptyJwksResponse() {
        // Arrange
        when(jwtService.generateJwkKey()).thenReturn(null);
        
        // Act
        JwksResponse response = authService.generateJwks();
        
        // Assert
        assertNotNull(response);
        assertTrue(response.getKeys().isEmpty());
    }

    @Test
    void generateJwks_WithException_ShouldReturnEmptyJwksResponse() {
        // Arrange
        when(jwtService.generateJwkKey()).thenThrow(new RuntimeException("Key generation failed"));
        
        // Act
        JwksResponse response = authService.generateJwks();
        
        // Assert
        assertNotNull(response);
        assertTrue(response.getKeys().isEmpty());
    }

    @Test
    void refreshToken_WithAccessTokenProcessingError_ShouldContinueRefresh() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String accessToken = "invalid-access-token";
        String userId = testUser.getId().toString();
        
        refreshTokenRequest.setAccessToken(accessToken);
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para getUserFromRefreshToken
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Mock para validateUserForRefresh
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(true);
        
        // Mock para processAccessTokenBlacklist - simula erro
        when(jwtService.extractUserId(accessToken)).thenThrow(new RuntimeException("Invalid access token"));
        
        // Mock para generateNewTokens
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtService.extractExpiration("new-access-token"))
            .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtService.convertToLocalDateTime(any()))
            .thenReturn(LocalDateTime.now().plusHours(1));

        // Act
        AuthResponse result = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
    }

    @Test
    void refreshToken_WithUserNotFound_ShouldThrowBadCredentialsException() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String userId = UUID.randomUUID().toString();
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para getUserFromRefreshToken - usuário não encontrado
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.empty());
        when(messageUtil.getMessage("service.auth.user.not.found"))
            .thenReturn("Usuário não encontrado");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
    void refreshToken_WithInvalidUserForRefresh_ShouldThrowBadCredentialsException() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String userId = testUser.getId().toString();
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para getUserFromRefreshToken
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Mock para validateUserForRefresh - token inválido para o usuário
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(false);
        when(messageUtil.getMessage("service.auth.refresh.token.invalid"))
            .thenReturn("Refresh token inválido");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
    void refreshToken_WithInactiveUserForRefresh_ShouldThrowBadCredentialsException() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String userId = testUser.getId().toString();
        testUser.setIsActive(false); // Usuário inativo
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para getUserFromRefreshToken
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Mock para validateUserForRefresh - token válido mas usuário inativo
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(true);
        when(messageUtil.getMessage("service.auth.user.inactive"))
            .thenReturn("Usuário inativo");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
    void introspect_WithValidClaimsButNotEmpty_ShouldReturnActiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken("valid-token");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");
        claims.put("exp", System.currentTimeMillis() / 1000 + 3600);
        claims.put("iat", System.currentTimeMillis() / 1000);
        
        when(jwtService.introspectToken("valid-token")).thenReturn(claims);
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertTrue(response.getActive());
        assertEquals(claims, response.getClaims());
    }

    @Test
    void introspect_WithNullClaims_ShouldReturnInactiveResponse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest();
        request.setToken("invalid-token");
        
        when(jwtService.introspectToken("invalid-token")).thenReturn(null);
        when(tokenValidationUtil.getTokenErrorMessage("invalid-token")).thenReturn("Token inválido");
        
        // Act
        TokenIntrospectResponse response = authService.introspect(request);
        
        // Assert
        assertFalse(response.getActive());
        assertEquals("Token inválido", response.getError());
    }

    @Test
    void refreshToken_WithValidAccessTokenForBlacklist_ShouldBlacklistAndContinue() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String accessToken = "valid-access-token";
        String userId = testUser.getId().toString();
        
        refreshTokenRequest.setAccessToken(accessToken);
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para processAccessTokenBlacklist - token válido
        when(jwtService.extractUserId(accessToken)).thenReturn(userId);
        when(jwtService.generateTokenHash(accessToken)).thenReturn("token-hash");
        when(jwtService.extractExpiration(accessToken))
            .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtService.convertToLocalDateTime(any()))
            .thenReturn(LocalDateTime.now().plusHours(1));
        
        // Mock para getUserFromRefreshToken
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Mock para validateUserForRefresh
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(true);
        
        // Mock para generateNewTokens
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtService.extractExpiration("new-access-token"))
            .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        
        // Act
        AuthResponse result = authService.refreshToken(refreshTokenRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(tokenBlacklistRepository).save(any(TokenBlacklist.class));
    }

    @Test
    void refreshToken_WithNullAccessToken_ShouldSkipBlacklistAndContinue() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String userId = testUser.getId().toString();
        
        refreshTokenRequest.setAccessToken(null);
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para getUserFromRefreshToken
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Mock para validateUserForRefresh
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(true);
        
        // Mock para generateNewTokens
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtService.extractExpiration("new-access-token"))
            .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtService.convertToLocalDateTime(any()))
            .thenReturn(LocalDateTime.now().plusHours(1));
        
        // Act
        AuthResponse result = authService.refreshToken(refreshTokenRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(tokenBlacklistRepository, never()).save(any(TokenBlacklist.class));
    }

    @Test
    void refreshToken_WithEmptyAccessToken_ShouldSkipBlacklistAndContinue() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String userId = testUser.getId().toString();
        
        refreshTokenRequest.setAccessToken("   "); // Token vazio com espaços
        
        // Mock tokenValidationUtil - usado em validateRefreshToken
        when(tokenValidationUtil.isTokenValid(eq(refreshToken), anyString())).thenReturn(true);
        when(tokenValidationUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        
        // Mock para getUserFromRefreshToken
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Mock para validateUserForRefresh
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(true);
        
        // Mock para generateNewTokens
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtService.extractExpiration("new-access-token"))
            .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtService.convertToLocalDateTime(any()))
            .thenReturn(LocalDateTime.now().plusHours(1));
        
        // Act
        AuthResponse result = authService.refreshToken(refreshTokenRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(tokenBlacklistRepository, never()).save(any(TokenBlacklist.class));
    }
}
