package br.com.systemrpg.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageUtilTest {

    @Mock
    private MessageSource messageSource;

    private MessageUtil messageUtil;

    @BeforeEach
    void setUp() {
        messageUtil = new MessageUtil(messageSource);
        LocaleContextHolder.setLocale(Locale.getDefault());
    }

    @Test
    void getMessage_WithKeyOnly_ShouldReturnLocalizedMessage() {
        String key = "test.message";
        String expectedMessage = "Test message";
        
        when(messageSource.getMessage(key, null, LocaleContextHolder.getLocale()))
            .thenReturn(expectedMessage);
        
        String result = messageUtil.getMessage(key);
        
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(key, null, LocaleContextHolder.getLocale());
    }

    @Test
    void getMessage_WithKeyAndArgs_ShouldReturnLocalizedMessageWithParameters() {
        String key = "test.message.with.args";
        Object[] args = {"param1", "param2"};
        String expectedMessage = "Test message with param1 and param2";
        
        when(messageSource.getMessage(key, args, LocaleContextHolder.getLocale()))
            .thenReturn(expectedMessage);
        
        String result = messageUtil.getMessage(key, args);
        
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Test
    void getMessage_WithKeyAndDefaultMessage_ShouldReturnLocalizedMessage() {
        String key = "test.message";
        String defaultMessage = "Default message";
        String expectedMessage = "Localized message";
        
        when(messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale()))
            .thenReturn(expectedMessage);
        
        String result = messageUtil.getMessage(key, defaultMessage);
        
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Test
    void getMessage_WithKeyDefaultMessageAndArgs_ShouldReturnLocalizedMessageWithParameters() {
        String key = "test.message.with.args";
        String defaultMessage = "Default message with {0} and {1}";
        Object[] args = {"param1", "param2"};
        String expectedMessage = "Localized message with param1 and param2";
        
        when(messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale()))
            .thenReturn(expectedMessage);
        
        String result = messageUtil.getMessage(key, defaultMessage, args);
        
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Test
    void getMessage_WithKeyAndDefaultMessage_WhenKeyNotFound_ShouldReturnDefaultMessage() {
        String key = "nonexistent.key";
        String defaultMessage = "Default message";
        
        when(messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale()))
            .thenReturn(defaultMessage);
        
        String result = messageUtil.getMessage(key, defaultMessage);
        
        assertEquals(defaultMessage, result);
        verify(messageSource).getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Test
    void getMessage_WithDifferentLocale_ShouldUseCurrentLocale() {
        Locale portugueseLocale = Locale.of("pt", "BR");
        LocaleContextHolder.setLocale(portugueseLocale);
        
        String key = "test.message";
        String expectedMessage = "Mensagem de teste";
        
        when(messageSource.getMessage(key, null, portugueseLocale))
            .thenReturn(expectedMessage);
        
        String result = messageUtil.getMessage(key);
        
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(key, null, portugueseLocale);
    }

    @Test
    void getMessage_WithEmptyArgs_ShouldHandleEmptyParameters() {
        String key = "test.message";
        Object[] emptyArgs = {};
        String expectedMessage = "Test message";
        
        when(messageSource.getMessage(key, emptyArgs, LocaleContextHolder.getLocale()))
            .thenReturn(expectedMessage);
        
        String result = messageUtil.getMessage(key, emptyArgs);
        
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(key, emptyArgs, LocaleContextHolder.getLocale());
    }

    @Test
    void getMessage_WithNullArgs_ShouldHandleNullParameters() {
        String key = "test.message";
        Object[] nullArgs = null;
        String expectedMessage = "Test message";
        
        when(messageSource.getMessage(key, nullArgs, LocaleContextHolder.getLocale()))
            .thenReturn(expectedMessage);
        
        String result = messageUtil.getMessage(key, nullArgs);
        
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(key, nullArgs, LocaleContextHolder.getLocale());
    }
}
