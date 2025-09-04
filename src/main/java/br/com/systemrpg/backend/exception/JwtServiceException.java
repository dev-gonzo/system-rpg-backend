package br.com.systemrpg.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando ocorrem erros durante operações com JWT.
 * Retorna status HTTP 500 (Internal Server Error).
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class JwtServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JwtServiceException(final String message) {
        super(message);
    }

    public JwtServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
