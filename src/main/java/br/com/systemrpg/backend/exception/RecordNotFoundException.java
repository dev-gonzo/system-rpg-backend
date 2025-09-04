package br.com.systemrpg.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um registro não é encontrado no banco de dados.
 * Retorna status HTTP 404 (Not Found).
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(final String message) {
        super(message);
    }

}
