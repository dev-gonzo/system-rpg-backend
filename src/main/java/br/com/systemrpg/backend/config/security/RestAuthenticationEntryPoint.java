package br.com.systemrpg.backend.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.systemrpg.backend.constants.MessageConstants;
import br.com.systemrpg.backend.exception.handler.RestResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Entry point responsável por tratar exceções de autenticação (401 Unauthorized).
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    private final MessageSource messageSource;



    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, 
                        final AuthenticationException exception) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        restResponse.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        restResponse.setMessage(messageSource.getMessage(MessageConstants.UNAUTHORIZED, null, LocaleContextHolder.getLocale()));
        restResponse.setPath(request.getRequestURI());
        
        String jsonResponse = objectMapper.writeValueAsString(restResponse);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(jsonResponse);
    }
}
