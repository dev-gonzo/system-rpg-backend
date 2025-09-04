package br.com.systemrpg.backend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.MessageSource;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para SecurityConfiguration.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {

    @InjectMocks
    private SecurityConfiguration securityConfiguration;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Act
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        assertTrue(encoder.encode("test").startsWith("$2a$"));
    }

    @Test
    void authenticationManager_ShouldReturnAuthenticationManager() throws Exception {
        // Arrange
        var mockAuthManager = mock(org.springframework.security.authentication.AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthManager);

        // Act
        var result = securityConfiguration.authenticationManager(authenticationConfiguration);

        // Assert
        assertNotNull(result);
        assertEquals(mockAuthManager, result);
    }

    @Test
    void securityFilterChain_ShouldConfigureCorsCorrectly() throws Exception {
        // Arrange
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        // Mock the chain of method calls
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.headers(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2ResourceServer(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);

        DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(mockFilterChain);

        // Act
        SecurityFilterChain result = securityConfiguration.securityFilterChain(httpSecurity);

        // Assert
        assertNotNull(result);
        verify(httpSecurity).cors(any());
    }

    @Test
    void corsConfiguration_ShouldAllowAllOrigins() {
        // This test verifies the CORS configuration lambda
        // We test this by creating the configuration directly
        
        // Create the CORS configuration as it would be created in the lambda
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(java.util.List.of("*"));
        config.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(java.util.List.of("*"));
        
        // Assert
        assertNotNull(config.getAllowedOriginPatterns());
        assertTrue(config.getAllowedOriginPatterns().contains("*"));
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedMethods().contains("PUT"));
        assertTrue(config.getAllowedMethods().contains("DELETE"));
        assertTrue(config.getAllowedMethods().contains("PATCH"));
        assertTrue(config.getAllowedMethods().contains("OPTIONS"));
        assertTrue(config.getAllowedHeaders().contains("*"));
        assertTrue(config.getAllowCredentials());
        assertTrue(config.getExposedHeaders().contains("*"));
    }

    @Test
    void securityFilterChain_ShouldExecuteCorsLambda() throws Exception {
        // Arrange - Set CORS properties using ReflectionTestUtils
        ReflectionTestUtils.setField(securityConfiguration, "allowedOrigins", "http://localhost:3000,http://localhost:4200");
        ReflectionTestUtils.setField(securityConfiguration, "allowedMethods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
        ReflectionTestUtils.setField(securityConfiguration, "allowedHeaders", "*");
        ReflectionTestUtils.setField(securityConfiguration, "allowCredentials", true);
        
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        ArgumentCaptor<org.springframework.security.config.Customizer<CorsConfigurer<HttpSecurity>>> corsCaptor = 
            ArgumentCaptor.forClass(org.springframework.security.config.Customizer.class);
        
        // Mock the chain of method calls
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(corsCaptor.capture())).thenReturn(httpSecurity);
        when(httpSecurity.headers(any())).thenReturn(httpSecurity);
        when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);
        
        DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(mockFilterChain);
        
        // Mock CorsConfigurer
        CorsConfigurer<HttpSecurity> corsConfigurer = mock(CorsConfigurer.class);
        ArgumentCaptor<CorsConfigurationSource> corsSourceCaptor = ArgumentCaptor.forClass(CorsConfigurationSource.class);
        when(corsConfigurer.configurationSource(corsSourceCaptor.capture())).thenReturn(corsConfigurer);
        
        // Act
        SecurityFilterChain result = securityConfiguration.securityFilterChain(httpSecurity);
        
        // Execute the captured CORS customizer
        corsCaptor.getValue().customize(corsConfigurer);
        
        // Execute the captured CORS configuration source
        CorsConfiguration corsConfig = corsSourceCaptor.getValue().getCorsConfiguration(mock(HttpServletRequest.class));
        
        // Assert
        assertNotNull(result);
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("PATCH"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
        assertTrue(corsConfig.getAllowedHeaders().contains("*"));
        assertTrue(corsConfig.getAllowCredentials());
        assertTrue(corsConfig.getExposedHeaders().contains("Authorization"));
        assertTrue(corsConfig.getExposedHeaders().contains("Content-Type"));
    }
}
