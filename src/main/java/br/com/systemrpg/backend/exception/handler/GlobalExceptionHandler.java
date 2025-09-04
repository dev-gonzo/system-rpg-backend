package br.com.systemrpg.backend.exception.handler;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;



/**
 * Handler global para tratamento de exceções gerais do sistema.
 * Complementa o RestExceptionHandler tratando exceções de segurança,
 * health check e outras exceções não específicas de negócio.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata exceções de autenticação.
     */
    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
    public ResponseEntity<RestResponse> handleAuthenticationException(
            final AuthenticationException exception,
            final WebRequest request) {

        log.warn("Authentication failed: {}", exception.getMessage());

        return RestResponse.entity(
                HttpStatus.UNAUTHORIZED,
                exception,
                "Credenciais inválidas",
                Collections.emptyList(),
                getPath(request)
        );
    }

    /**
     * Trata exceções de autorização (acesso negado).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestResponse> handleAccessDeniedException(
            final AccessDeniedException exception,
            final WebRequest request) {

        log.warn("Access denied: {}", exception.getMessage());

        return RestResponse.entity(
                HttpStatus.FORBIDDEN,
                exception,
                "Acesso negado",
                Collections.emptyList(),
                getPath(request)
        );
    }

    /**
     * Trata exceções de recurso não encontrado (404 Not Found).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestResponse> handleNoResourceFoundException(
            final NoResourceFoundException exception,
            final WebRequest request) {

        log.warn("Resource not found: {}", exception.getMessage());

        return RestResponse.entity(
                HttpStatus.NOT_FOUND,
                exception,
                "Recurso não encontrado",
                Collections.emptyList(),
                getPath(request)
        );
    }

    /**
     * Extrai o caminho da requisição.
     */
    private String getPath(final WebRequest request) {
        return request.getDescription(false).substring(4);
    }
}
