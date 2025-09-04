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
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.systemrpg.backend.constants.MessageConstants;
import br.com.systemrpg.backend.exception.handler.RestResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Testes unitários para {@link RestAccessDeniedHandler}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RestAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessDeniedException accessDeniedException;

    @InjectMocks
    private RestAccessDeniedHandler restAccessDeniedHandler;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testHandle_ShouldSetCorrectResponseProperties() throws IOException {
        // Given
        String requestUri = "/api/test";
        String forbiddenMessage = "Acesso negado";
        String jsonResponse = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Acesso negado\",\"path\":\"/api/test\"}";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, locale))
            .thenReturn(forbiddenMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn(jsonResponse);

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAccessDeniedHandler.handle(request, response, accessDeniedException);
        }

        // Then
        verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        
        printWriter.flush();
        assertEquals(jsonResponse, stringWriter.toString());
    }

    @Test
    void testHandle_ShouldCreateCorrectRestResponse() throws IOException {
        // Given
        String requestUri = "/api/protected";
        String forbiddenMessage = "Access denied";
        Locale locale = Locale.ENGLISH;

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, locale))
            .thenReturn(forbiddenMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAccessDeniedHandler.handle(request, response, accessDeniedException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return resp.getStatus() == HttpStatus.FORBIDDEN.value() &&
                   HttpStatus.FORBIDDEN.getReasonPhrase().equals(resp.getError()) &&
                   forbiddenMessage.equals(resp.getMessage()) &&
                   requestUri.equals(resp.getPath());
        }));
    }

    @Test
    void testHandle_WithDifferentLocale() throws IOException {
        // Given
        String requestUri = "/api/admin";
        String forbiddenMessage = "Accès refusé";
        Locale frenchLocale = Locale.FRENCH;

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, frenchLocale))
            .thenReturn(forbiddenMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(frenchLocale);
            
            restAccessDeniedHandler.handle(request, response, accessDeniedException);
        }

        // Then
        verify(messageSource).getMessage(MessageConstants.FORBIDDEN, null, frenchLocale);
    }

    @Test
    void testHandle_WithNullRequestUri() throws IOException {
        // Given
        String forbiddenMessage = "Acesso negado";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(null);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, locale))
            .thenReturn(forbiddenMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAccessDeniedHandler.handle(request, response, accessDeniedException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return resp.getPath() == null;
        }));
    }

    @Test
    void testHandle_WithEmptyRequestUri() throws IOException {
        // Given
        String requestUri = "";
        String forbiddenMessage = "Acesso negado";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, locale))
            .thenReturn(forbiddenMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn("{}");

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAccessDeniedHandler.handle(request, response, accessDeniedException);
        }

        // Then
        verify(objectMapper).writeValueAsString(argThat(restResponse -> {
            RestResponse resp = (RestResponse) restResponse;
            return "".equals(resp.getPath());
        }));
    }

    @Test
    void testHandle_WhenObjectMapperThrowsException() throws IOException {
        // Given
        String requestUri = "/api/test";
        String forbiddenMessage = "Acesso negado";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, locale))
            .thenReturn(forbiddenMessage);
        doThrow(new JsonProcessingException("JSON serialization error") {})
            .when(objectMapper).writeValueAsString(any(RestResponse.class));

        // When & Then
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            assertThrows(JsonProcessingException.class, () -> {
                restAccessDeniedHandler.handle(request, response, accessDeniedException);
            });
        }
    }

    @Test
    void testHandle_WhenResponseWriterThrowsException() throws IOException {
        // Given
        String requestUri = "/api/test";
        String forbiddenMessage = "Acesso negado";
        String jsonResponse = "{\"status\":403}";
        Locale locale = Locale.getDefault();

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, locale))
            .thenReturn(forbiddenMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn(jsonResponse);
        when(response.getWriter()).thenThrow(new IOException("Writer error"));

        // When & Then
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            assertThrows(IOException.class, () -> {
                restAccessDeniedHandler.handle(request, response, accessDeniedException);
            });
        }
    }

    @Test
    void testHandle_VerifyAllInteractions() throws IOException {
        // Given
        String requestUri = "/api/secure";
        String forbiddenMessage = "Forbidden access";
        String jsonResponse = "{\"message\":\"forbidden\"}";
        Locale locale = Locale.US;

        when(request.getRequestURI()).thenReturn(requestUri);
        when(messageSource.getMessage(MessageConstants.FORBIDDEN, null, locale))
            .thenReturn(forbiddenMessage);
        when(objectMapper.writeValueAsString(any(RestResponse.class)))
            .thenReturn(jsonResponse);

        // When
        try (MockedStatic<LocaleContextHolder> mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            restAccessDeniedHandler.handle(request, response, accessDeniedException);
        }

        // Then
        verify(request).getRequestURI();
        verify(messageSource).getMessage(MessageConstants.FORBIDDEN, null, locale);
        verify(objectMapper).writeValueAsString(any(RestResponse.class));
        verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).getWriter();
        
        printWriter.flush();
        assertEquals(jsonResponse, stringWriter.toString());
    }

    @Test
    void testClassAnnotations() {
        // Then
        assertTrue(RestAccessDeniedHandler.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        // Note: Lombok annotations are processed at compile time and not available at runtime
        // So we verify the class has the expected constructor instead
        assertTrue(RestAccessDeniedHandler.class.getConstructors().length > 0);
    }

    @Test
    void testImplementsAccessDeniedHandler() {
        // Then
        assertTrue(AccessDeniedHandler.class.isAssignableFrom(RestAccessDeniedHandler.class));
    }

    @Test
    void testConstructor() {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        MessageSource msgSource = mock(MessageSource.class);

        // When
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler(mapper, msgSource);

        // Then
        assertNotNull(handler);
    }
}
