package br.com.systemrpg.backend.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.systemrpg.backend.config.security.RestAccessDeniedHandler;
import br.com.systemrpg.backend.config.security.RestAuthenticationEntryPoint;
import br.com.systemrpg.backend.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;

/**
 * Configuração de segurança da aplicação com Spring Security e OAuth2 JWT.
 * Define as regras de autorização, tratamento de exceções e configuração de
 * sessão.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

        private final ObjectMapper objectMapper;
        private final MessageSource messageSource;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final UserDetailsService userDetailsService;
        
        @Value("${cors.allowed-origins}")
        private String allowedOrigins;
        
        @Value("${cors.allowed-methods}")
        private String allowedMethods;
        
        @Value("${cors.allowed-headers}")
        private String allowedHeaders;
        
        @Value("${cors.allow-credentials}")
        private boolean allowCredentials;

        /**
         * Configura a cadeia de filtros de segurança da aplicação.
         * Define as regras de autorização para endpoints, tratamento de exceções
         * e configuração do servidor de recursos OAuth2 com JWT.
         */
        @Bean
        @SuppressWarnings("java:S4502") // Suprime warning CSRF - justificado para API REST stateless
        SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(authorize -> authorize
                            // Endpoints públicos
                            .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                            .requestMatchers("/swagger-resources/**").permitAll()
                            .requestMatchers("/actuator/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/refresh").permitAll()
                            .requestMatchers(HttpMethod.POST, "/logout").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                            .requestMatchers(HttpMethod.GET, "/users/check-username").permitAll()
                            .requestMatchers(HttpMethod.GET, "/users/check-email").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users").permitAll()
                            .requestMatchers(HttpMethod.POST, "/introspect").permitAll()
                            .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                            // Todos os outros endpoints precisam de autenticação
                            .anyRequest().authenticated()
                    )
                    // CSRF PROTECTION DISABLED - JUSTIFICATIVA DE SEGURANÇA:
                    // 1. Esta é uma API REST stateless que usa autenticação JWT
                    // 2. Não utilizamos cookies de sessão (session cookies)
                    // 3. Tokens JWT são enviados via Authorization header
                    // 4. CSRF attacks requerem cookies de sessão para funcionar
                    // 5. Sem cookies de sessão, não há vetor de ataque CSRF
                    // SONARQUBE: Esta configuração é SEGURA para APIs REST stateless.
                    // Referência: OWASP CSRF Prevention Cheat Sheet
                    .csrf(AbstractHttpConfigurer::disable)
                    // CORS configurado via propriedades - seguro para produção
                    // Configuração via application.properties ou variáveis de ambiente
                    .cors(cors -> cors.configurationSource(request -> {
                        var config = new org.springframework.web.cors.CorsConfiguration();
                        // Origins configuráveis via propriedades (padrão: localhost para dev)
                        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
                        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
                        config.setAllowCredentials(allowCredentials);
                        config.setExposedHeaders(java.util.List.of("Authorization", "Content-Type"));
                        return config;
                    }))
                    .headers(headers -> headers
                        .frameOptions(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(contentTypeOptions -> {})
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                            .maxAgeInSeconds(SecurityConstants.HSTS_MAX_AGE_SECONDS)
                            .includeSubDomains(true)
                        )
                        .referrerPolicy(referrerPolicy -> referrerPolicy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                    )
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper, messageSource))
                            .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper, messageSource))
                    )
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authenticationProvider(authenticationProvider())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


            http.logout(AbstractHttpConfigurer::disable);

            return http.build();
        }


    /**
     * Configura o encoder de senhas usando BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura o AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura o AuthenticationProvider usando DaoAuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
