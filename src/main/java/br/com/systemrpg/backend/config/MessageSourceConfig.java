package br.com.systemrpg.backend.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import br.com.systemrpg.backend.constants.SecurityConstants;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Configuração completa de internacionalização da aplicação.
 * Define o carregamento dos arquivos de mensagens, configurações de encoding
 * e resolução de locale baseada no header Accept-Language.
 */
@Configuration
public class MessageSourceConfig {

    /**
     * Configura o MessageSource para carregar mensagens de internacionalização.
     * 
     * @return MessageSource configurado
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds((int) SecurityConstants.MESSAGE_CACHE_SECONDS); // Cache por 1 hora
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Configura o LocaleResolver para determinar o locale baseado no header Accept-Language.
     * Suporta português (pt-BR) como padrão e inglês (en) como alternativa.
     * 
     * @return LocaleResolver configurado
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.of("pt", "BR")); // Português brasileiro como padrão
        localeResolver.setSupportedLocales(java.util.List.of(
            Locale.of("pt", "BR"), // Português brasileiro
            Locale.of("en", "US"),  // Inglês americano
            Locale.of("es", "ES")   // Espanhol
        ));
        return localeResolver;
    }
}
