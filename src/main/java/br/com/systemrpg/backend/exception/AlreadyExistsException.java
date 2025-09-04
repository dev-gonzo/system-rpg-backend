package br.com.systemrpg.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando se tenta criar um registro que já existe no sistema.
 * Retorna status HTTP 400 (Bad Request).
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class AlreadyExistsException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AlreadyExistsException(final String message) {
        super(message);
    }

    public AlreadyExistsException(final String entityName, final Object value) {
        super(String.format("%s com valor '%s' já existe no sistema", entityName, value));
    }
}
