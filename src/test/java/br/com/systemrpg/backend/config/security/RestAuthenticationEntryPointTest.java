package br.com.systemrpg.backend.config.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.systemrpg.backend.constants.MessageConstants;
import br.com.systemrpg.backend.exception.handler.RestResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Testes unitários para {@link RestAuthenticationEntryPoint}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RestAuthenticationEntryPointTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authenticationException;

    @InjectMocks
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testCommence_ShouldSetCorrectResponseProperties() throws IOException {
        // Given
        String requestUri = "/api/login";
        String unauthorizedMessage = "Não autorizado";
        String jsonResponse = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Não autorizado\",\"path\":\"/api/login\"}";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn(jsonResponse);

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAuthenticationEntryPoint.commence(request, response, authenticationException);
        }

        // Then
        verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        
        printWriter.flush();
        assertEquals(jsonResponse, stringWriter.toString());
    }

    @Test
    void testCommence_ShouldCreateCorrectRestResponse() throws IOException {
        // Given
        String requestUri = "/api/protected";
        String unauthorizedMessage = "Authentication required";
        Locale locale = Locale.ENGLISH;

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAuthenticationEntryPoint.commence(request, response, authenticationException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return resp.getStatus() == HttpStatus.UNAUTHORIZED.value() &&
                   HttpStatus.UNAUTHORIZED.getReasonPhrase().equals(resp.getError()) &&
                   unauthorizedMessage.equals(resp.getMessage()) &&
                   requestUri.equals(resp.getPath());
        }));
    }

    @Test
    void testCommence_WithDifferentLocale() throws IOException {
        // Given
        String requestUri = "/api/secure";
        String unauthorizedMessage = "Non autorisé";
        Locale frenchLocale = Locale.FRENCH;

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, frenchLocale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(frenchLocale);
            
            restAuthenticationEntryPoint.commence(request, response, authenticationException);
        }

        // Then
        verify(messageSource).getMessage(MessageConstants.UNAUTHORIZED, null, frenchLocale);
    }

    @Test
    void testCommence_WithNullRequestUri() throws IOException {
        // Given
        String unauthorizedMessage = "Não autorizado";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(null);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAuthenticationEntryPoint.commence(request, response, authenticationException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return resp.getPath() == null;
        }));
    }

    @Test
    void testCommence_WithEmptyRequestUri() throws IOException {
        // Given
        String requestUri = "";
        String unauthorizedMessage = "Não autorizado";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAuthenticationEntryPoint.commence(request, response, authenticationException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return "".equals(resp.getPath());
        }));
    }

    @Test
    void testCommence_WhenObjectMapperThrowsException() throws IOException {
        // Given
        String requestUri = "/api/test";
        String unauthorizedMessage = "Não autorizado";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        doThrow(new JsonProcessingException("JSON serialization error") {})
            .when(objectMapper).writeValueAsString(any(RestResponse.class));

        // When & Then
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            assertThrows(JsonProcessingException.class, () -> {
                restAuthenticationEntryPoint.commence(request, response, authenticationException);
            });
        }
    }

    @Test
    void testCommence_WhenResponseWriterThrowsException() throws IOException {
        // Given
        String requestUri = "/api/test";
        String unauthorizedMessage = "Não autorizado";
        String jsonResponse = "{\"status\":401}";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn(jsonResponse);
        when(response.getWriter()).thenThrow(new IOException("Writer error"));

        // When & Then
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            assertThrows(IOException.class, () -> {
                restAuthenticationEntryPoint.commence(request, response, authenticationException);
            });
        }
    }

    @Test
    void testCommence_VerifyAllInteractions() throws IOException {
        // Given
        String requestUri = "/api/auth";
        String unauthorizedMessage = "Authentication failed";
        String jsonResponse = "{\"message\":\"unauthorized\"}";
        Locale locale = Locale.US;

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn(jsonResponse);

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAuthenticationEntryPoint.commence(request, response, authenticationException);
        }

        // Then
        verify(request).getRequestURI();
        verify(messageSource).getMessage(MessageConstants.UNAUTHORIZED, null, locale);
        verify(objectMapper).writeValueAsString(any(RestResponse.class));
        verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).getWriter();
        
        printWriter.flush();
        assertEquals(jsonResponse, stringWriter.toString());
    }

    @Test
    void testCommence_WithDifferentAuthenticationExceptions() throws IOException {
        // Given
        String requestUri = "/api/login";
        String unauthorizedMessage = "Bad credentials";
        Locale locale = Locale.getDefault();
        
        AuthenticationException badCredentialsException = mock(AuthenticationException.class);
        when(badCredentialsException.getMessage()).thenReturn("Invalid username or password");

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAuthenticationEntryPoint.commence(request, response, badCredentialsException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return resp.getStatus() == HttpStatus.UNAUTHORIZED.value() &&
                   HttpStatus.UNAUTHORIZED.getReasonPhrase().equals(resp.getError()) &&
                   unauthorizedMessage.equals(resp.getMessage()) &&
                   requestUri.equals(resp.getPath());
        }));
    }

    @Test
    void testClassAnnotations() {
        // Then
        assertTrue(RestAuthenticationEntryPoint.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        // Note: Lombok annotations are processed at compile time and not available at runtime
        // So we verify the class has the expected constructor instead
        assertTrue(RestAuthenticationEntryPoint.class.getConstructors().length > 0);
    }

    @Test
    void testImplementsAuthenticationEntryPoint() {
        // Then
        assertTrue(AuthenticationEntryPoint.class.isAssignableFrom(RestAuthenticationEntryPoint.class));
    }

    @Test
    void testConstructor() {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        MessageSource msgSource = mock(MessageSource.class);

        // When
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(mapper, msgSource);

        // Then
        assertNotNull(entryPoint);
    }

    @Test
    void testCommence_WithLongRequestUri() throws IOException {
        // Given
        String longRequestUri = "/api/very/long/path/with/many/segments/and/parameters?param1=value1&param2=value2&param3=value3";
        String unauthorizedMessage = "Não autorizado";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(longRequestUri);
        when(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, locale))
            .thenReturn(unauthorizedMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAuthenticationEntryPoint.commence(request, response, authenticationException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return longRequestUri.equals(resp.getPath());
        }));
    }
}
