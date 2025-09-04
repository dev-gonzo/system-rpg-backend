package br.com.systemrpg.backend.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Utilitário para centralizar a obtenção de mensagens internacionalizadas.
 * Reduz a duplicação de código ao acessar o MessageSource.
 */
@Component
@RequiredArgsConstructor
public class MessageUtil {

    private final MessageSource messageSource;

    /**
     * Obtém uma mensagem internacionalizada sem parâmetros.
     *
     * @param key a chave da mensagem
     * @return a mensagem localizada
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Obtém uma mensagem internacionalizada com parâmetros.
     *
     * @param key a chave da mensagem
     * @param args os argumentos para a mensagem
     * @return a mensagem localizada
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Obtém uma mensagem internacionalizada com valor padrão.
     *
     * @param key a chave da mensagem
     * @param defaultMessage a mensagem padrão se a chave não for encontrada
     * @return a mensagem localizada ou a mensagem padrão
     */
    public String getMessage(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Obtém uma mensagem internacionalizada com parâmetros e valor padrão.
     *
     * @param key a chave da mensagem
     * @param defaultMessage a mensagem padrão se a chave não for encontrada
     * @param args os argumentos para a mensagem
     * @return a mensagem localizada ou a mensagem padrão
     */
    public String getMessage(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}
