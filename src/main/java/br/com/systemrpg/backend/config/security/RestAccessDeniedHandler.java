package br.com.systemrpg.backend.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.systemrpg.backend.constants.MessageConstants;
import br.com.systemrpg.backend.exception.handler.RestResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handler responsável por tratar exceções de acesso negado (403 Forbidden).
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    private final MessageSource messageSource;



    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, 
                      final AccessDeniedException exception) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(HttpStatus.FORBIDDEN.value());
        restResponse.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        restResponse.setMessage(messageSource.getMessage(MessageConstants.FORBIDDEN, null, LocaleContextHolder.getLocale()));
        restResponse.setPath(request.getRequestURI());
        
        String jsonResponse = objectMapper.writeValueAsString(restResponse);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(jsonResponse);
    }
}
