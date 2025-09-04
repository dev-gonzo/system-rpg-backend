package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.dto.AuthResponse;
import br.com.systemrpg.backend.dto.hateoas.AuthHateoasResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe AuthHateoasMapper.
 */
class AuthHateoasMapperTest {

    private AuthHateoasMapper authHateoasMapper;
    private AuthResponse authResponse;
    private AuthResponse.UserInfo userInfo;

    @BeforeEach
    void setUp() {
        authHateoasMapper = Mappers.getMapper(AuthHateoasMapper.class);
        
        // Setup test user info
        userInfo = AuthResponse.UserInfo.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(List.of("ROLE_USER", "ROLE_ADMIN"))
                .isActive(true)
                .isEmailVerified(true)
                .lastLoginAt(LocalDateTime.now().minusHours(1))
                .build();
        
        // Setup test auth response
        authResponse = AuthResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .refreshToken("refresh_token_value")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .user(userInfo)
                .build();
    }

    @Test
    void toHateoasResponse_ShouldMapAuthResponseToAuthHateoasResponse() {
        // Act
        AuthHateoasResponse result = authHateoasMapper.toHateoasResponse(authResponse);

        // Assert
        assertNotNull(result);
        assertEquals(result.getAccessToken(), authResponse.getAccessToken());
        assertEquals(result.getRefreshToken(), authResponse.getRefreshToken());
        assertEquals(result.getTokenType(), authResponse.getTokenType());
        assertEquals(result.getExpiresIn(), authResponse.getExpiresIn());
        assertEquals(result.getExpiresAt(), authResponse.getExpiresAt());
        
        // Verify user info mapping
        assertNotNull(result.getUser());
        assertEquals(result.getUser().getId(), userInfo.getId());
        assertEquals(result.getUser().getUsername(), userInfo.getUsername());
        assertEquals(result.getUser().getEmail(), userInfo.getEmail());
        assertEquals(result.getUser().getFirstName(), userInfo.getFirstName());
        assertEquals(result.getUser().getLastName(), userInfo.getLastName());
        assertEquals(result.getUser().getRoles(), userInfo.getRoles());
        assertEquals(result.getUser().getIsActive(), userInfo.getIsActive());
        assertEquals(result.getUser().getIsEmailVerified(), userInfo.getIsEmailVerified());
        assertEquals(result.getUser().getLastLoginAt(), userInfo.getLastLoginAt());
        
        // Verify links are initialized (ignored in mapping but should be present)
        assertNotNull(result.getLinks());
        assertTrue(result.getLinks().isEmpty());
    }

    @Test
    void toHateoasResponse_WithNullAuthResponse_ShouldReturnNull() {
        // Act
        AuthHateoasResponse result = authHateoasMapper.toHateoasResponse(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toHateoasResponse_WithNullUserInfo_ShouldMapOtherFields() {
        // Arrange
        AuthResponse authResponseWithNullUser = AuthResponse.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .user(null)
                .build();

        // Act
        AuthHateoasResponse result = authHateoasMapper.toHateoasResponse(authResponseWithNullUser);

        // Assert
        assertNotNull(result);
        assertEquals("access_token", result.getAccessToken());
        assertEquals("refresh_token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());
        assertNotNull(result.getExpiresAt());
        assertNull(result.getUser());
    }

    @Test
    void toHateoasUserInfo_ShouldMapUserInfoToHateoasUserInfo() {
        // Act
        AuthHateoasResponse.UserInfo result = authHateoasMapper.toHateoasUserInfo(userInfo);

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), userInfo.getId());
        assertEquals(result.getUsername(), userInfo.getUsername());
        assertEquals(result.getEmail(), userInfo.getEmail());
        assertEquals(result.getFirstName(), userInfo.getFirstName());
        assertEquals(result.getLastName(), userInfo.getLastName());
        assertEquals(result.getRoles(), userInfo.getRoles());
        assertEquals(result.getIsActive(), userInfo.getIsActive());
        assertEquals(result.getIsEmailVerified(), userInfo.getIsEmailVerified());
        assertEquals(result.getLastLoginAt(), userInfo.getLastLoginAt());
    }

    @Test
    void toHateoasUserInfo_WithNullUserInfo_ShouldReturnNull() {
        // Act
        AuthHateoasResponse.UserInfo result = authHateoasMapper.toHateoasUserInfo(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toHateoasUserInfo_WithMinimalUserInfo_ShouldMapCorrectly() {
        // Arrange
        AuthResponse.UserInfo minimalUserInfo = AuthResponse.UserInfo.builder()
                .id(UUID.randomUUID())
                .username("minimal")
                .email("minimal@example.com")
                .isActive(false)
                .isEmailVerified(false)
                .build();

        // Act
        AuthHateoasResponse.UserInfo result = authHateoasMapper.toHateoasUserInfo(minimalUserInfo);

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), minimalUserInfo.getId());
        assertEquals(result.getUsername(), minimalUserInfo.getUsername());
        assertEquals(result.getEmail(), minimalUserInfo.getEmail());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getRoles());
        assertFalse(result.getIsActive());
        assertFalse(result.getIsEmailVerified());
        assertNull(result.getLastLoginAt());
    }

    @Test
    void toHateoasResponse_WithEmptyRoles_ShouldMapCorrectly() {
        // Arrange
        AuthResponse.UserInfo userInfoWithEmptyRoles = AuthResponse.UserInfo.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(List.of()) // Empty roles
                .isActive(true)
                .isEmailVerified(true)
                .build();
        
        AuthResponse authResponseWithEmptyRoles = AuthResponse.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .user(userInfoWithEmptyRoles)
                .build();

        // Act
        AuthHateoasResponse result = authHateoasMapper.toHateoasResponse(authResponseWithEmptyRoles);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUser());
        assertNotNull(result.getUser().getRoles());
        assertTrue(result.getUser().getRoles().isEmpty());
    }

    @Test
    void toHateoasResponse_WithLongTokens_ShouldMapCorrectly() {
        // Arrange
        String longAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String longRefreshToken = "refresh_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        AuthResponse authResponseWithLongTokens = AuthResponse.builder()
                .accessToken(longAccessToken)
                .refreshToken(longRefreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .expiresAt(LocalDateTime.now().plusHours(2))
                .user(userInfo)
                .build();

        // Act
        AuthHateoasResponse result = authHateoasMapper.toHateoasResponse(authResponseWithLongTokens);

        // Assert
        assertNotNull(result);
        assertEquals(longAccessToken, result.getAccessToken());
        assertEquals(longRefreshToken, result.getRefreshToken());
        assertEquals(7200L, result.getExpiresIn());
        assertNotNull(result.getExpiresAt());
    }
}
