package br.com.systemrpg.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.JwkKey;
import br.com.systemrpg.backend.dto.JwksResponse;
import br.com.systemrpg.backend.dto.LoginRequest;
import br.com.systemrpg.backend.dto.AuthResponse;
import br.com.systemrpg.backend.dto.RefreshTokenRequest;
import br.com.systemrpg.backend.dto.TokenIntrospectRequest;
import br.com.systemrpg.backend.dto.TokenIntrospectResponse;
import br.com.systemrpg.backend.dto.hateoas.AuthHateoasResponse;
import br.com.systemrpg.backend.exception.BusinessException;
import br.com.systemrpg.backend.hateoas.HateoasLinkBuilder;
import br.com.systemrpg.backend.mapper.AuthHateoasMapper;
import br.com.systemrpg.backend.config.TestSecurityConfig;
import br.com.systemrpg.backend.service.AuthService;
import br.com.systemrpg.backend.service.JwtService;
import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.TokenValidationUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
private JwtService jwtService;

    @MockitoBean
    private AuthHateoasMapper authHateoasMapper;

    @MockitoBean
    private HateoasLinkBuilder hateoasLinkBuilder;

    @MockitoBean
    private MessageSource messageSource;

    @MockitoBean
    private MessageUtil messageUtil;

    @MockitoBean
    private TokenValidationUtil tokenValidationUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private AuthResponse loginResponse;
    private RefreshTokenRequest refreshTokenRequest;
    private TokenIntrospectRequest introspectionRequest;
    private User testUser;
    private Role testRole;

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
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(testRole));
        testUser.setCreatedAt(LocalDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        loginResponse = AuthResponse.builder()
            .accessToken("access_token_123")
            .refreshToken("refresh_token_123")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .build();

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh_token_123");
        refreshTokenRequest.setAccessToken("access_token_123");

        introspectionRequest = new TokenIntrospectRequest();
        introspectionRequest.setToken("token_to_introspect");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() throws Exception {
        // Arrange
        AuthHateoasResponse hateoasResponse = AuthHateoasResponse.builder()
            .accessToken("access_token_123")
            .refreshToken("refresh_token_123")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .build();
            
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);
        when(authHateoasMapper.toHateoasResponse(any(AuthResponse.class))).thenReturn(hateoasResponse);

        // Act & Assert
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token_123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token_123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authService).login(any(LoginRequest.class));
        verify(authHateoasMapper).toHateoasResponse(any(AuthResponse.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        // Act & Assert
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidRequestBody_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        // Username e password são obrigatórios

        // Act & Assert
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() throws Exception {
        // Arrange
        AuthHateoasResponse hateoasResponse = AuthHateoasResponse.builder()
            .accessToken("access_token_123")
            .refreshToken("refresh_token_123")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .build();
            
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(loginResponse);
        when(authHateoasMapper.toHateoasResponse(any(AuthResponse.class))).thenReturn(hateoasResponse);

        // Act & Assert
        mockMvc.perform(post("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token_123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token_123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
        verify(authHateoasMapper).toHateoasResponse(any(AuthResponse.class));
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authService.refreshToken(any(RefreshTokenRequest.class)))
            .thenThrow(new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        // Act & Assert
        mockMvc.perform(post("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void introspectToken_WithValidToken_ShouldReturnTokenInfo() throws Exception {
        // Arrange
        Map<String, Object> claims = Map.of(
            "username", "testuser",
            "exp", 1234567890L,
            "iat", 1234564290L
        );
        TokenIntrospectResponse response = TokenIntrospectResponse.active(claims);
        when(authService.introspect(any(TokenIntrospectRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(introspectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.claims.username").value("testuser"))
                .andExpect(jsonPath("$.claims.exp").value(1234567890L));

        verify(authService).introspect(any(TokenIntrospectRequest.class));
    }

    @Test
    void introspectToken_WithInvalidToken_ShouldReturnInactiveStatus() throws Exception {
        // Arrange
        TokenIntrospectResponse response = TokenIntrospectResponse.inactive("Token inválido");
        when(authService.introspect(any(TokenIntrospectRequest.class))).thenReturn(response);

        introspectionRequest.setToken("invalid_token");

        // Act & Assert
        mockMvc.perform(post("/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(introspectionRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.error").value("Token inválido"));

        verify(authService).introspect(any(TokenIntrospectRequest.class));
    }

    @Test
    void logout_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Arrange
        String validToken = "Bearer valid-token";
        doNothing().when(authService).logout(anyString());
        when(messageUtil.getMessage("controller.auth.logout.success"))
                .thenReturn("Logout realizado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/logout")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout realizado com sucesso"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).logout(validToken);
    }

    @Test
    void logout_WithoutAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(messageUtil.getMessage("controller.auth.logout.error"))
                .thenReturn("Erro no logout");

        // Act & Assert
        mockMvc.perform(post("/logout"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).logout(anyString());
    }

    @Test
    void logout_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new BusinessException("Invalid token", HttpStatus.BAD_REQUEST))
            .when(authService).logout("Bearer invalid_token");
        when(messageUtil.getMessage("controller.auth.logout.error"))
                .thenReturn("Erro no logout");

        // Act & Assert
        mockMvc.perform(post("/logout")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isBadRequest());

        verify(authService).logout("Bearer invalid_token");
    }

    @Test
    void login_WithMissingUsername_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setPassword("password123");
        // Username está faltando

        // Act & Assert
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithMissingPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("testuser");
        // Password está faltando

        // Act & Assert
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void refreshToken_WithMissingRefreshToken_ShouldReturnBadRequest() throws Exception {
        // Arrange
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest();
        invalidRequest.setAccessToken("access_token_123");
        // RefreshToken está faltando

        // Act & Assert
        mockMvc.perform(post("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void introspectToken_WithException_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authService.introspect(any(TokenIntrospectRequest.class)))
            .thenThrow(new RuntimeException("Database connection error"));
        when(messageUtil.getMessage("controller.auth.introspect.error"))
            .thenReturn("Erro interno durante introspecção");

        // Act & Assert
        mockMvc.perform(post("/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(introspectionRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.error").value("Erro interno durante introspecção"));

        verify(authService).introspect(any(TokenIntrospectRequest.class));
        verify(messageUtil).getMessage("controller.auth.introspect.error");
    }

    @Test
    void jwks_WithValidRequest_ShouldReturnJwksResponse() throws Exception {
        // Arrange
        JwkKey jwkKey = JwkKey.builder()
            .keyType("RSA")
            .keyUse("sig")
            .keyId("key-1")
            .algorithm("RS256")
            .modulus("test-modulus")
            .exponent("AQAB")
            .build();
        
        JwksResponse jwksResponse = JwksResponse.of(jwkKey);
        when(authService.generateJwks()).thenReturn(jwksResponse);

        // Act & Assert
        mockMvc.perform(get("/.well-known/jwks.json")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].kid").value("key-1"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"));

        verify(authService).generateJwks();
    }

    @Test
    void jwks_WithException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(authService.generateJwks())
            .thenThrow(new RuntimeException("Key generation failed"));

        // Act & Assert
        mockMvc.perform(get("/.well-known/jwks.json")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys").isEmpty());

        verify(authService).generateJwks();
    }
}
