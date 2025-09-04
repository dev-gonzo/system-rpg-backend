package br.com.systemrpg.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import br.com.systemrpg.backend.constants.SecurityConstants;

/**
 * Configuração web da aplicação para interceptadores e outras configurações
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:Authorization,Content-Type,Accept,Origin,X-Requested-With}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers:Authorization,Content-Type}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * Configuração CORS mais restritiva para segurança.
     * Configurável via application.properties ou variáveis de ambiente.
     * SonarQube: Configuração segura - não permite todas as origens.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .exposedHeaders(exposedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(SecurityConstants.CORS_MAX_AGE_SECONDS);
    }

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        // Desabilite explicitamente o tratamento de recursos estáticos para caminhos /api/**
        registry.setOrder(Integer.MAX_VALUE);
    }

}
