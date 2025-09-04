package br.com.systemrpg.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção de negócio customizada para tratar erros relacionados às regras de
 * negócio da aplicação.
 * Esta exceção permite especificar um status HTTP personalizado.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus httpStatus;

    public BusinessException(final String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(final String message, final HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
