package br.com.systemrpg.backend.exception.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.systemrpg.backend.constants.MessageConstants;
import br.com.systemrpg.backend.exception.AlreadyExistsException;
import br.com.systemrpg.backend.exception.BusinessException;
import br.com.systemrpg.backend.exception.RecordNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Handler global para tratamento de exceções da aplicação.
 */
@ControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<RestResponse> handleBusiness(final WebRequest request, final BusinessException exception) {
        String message = getMessage(exception.getMessage());
        List<RestResponse.FieldError> fieldErrors = Collections.emptyList();

        return handleInvalidParamException(request, exception.getHttpStatus(), exception, message, fieldErrors);
    }

    @ExceptionHandler(RecordNotFoundException.class)
    ResponseEntity<RestResponse> handleNotFound(final WebRequest request, final RecordNotFoundException exception) {
        String message = getMessage(exception.getMessage());
        List<RestResponse.FieldError> fieldErrors = Collections.emptyList();

        return handleInvalidParamException(request, HttpStatus.NOT_FOUND, exception, message, fieldErrors);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    ResponseEntity<RestResponse> handleAlreadyExists(final WebRequest request, final AlreadyExistsException exception) {
        String message = getMessage(exception.getMessage());
        List<RestResponse.FieldError> fieldErrors = Collections.emptyList();

        return handleInvalidParamException(request, HttpStatus.BAD_REQUEST, exception, message, fieldErrors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<RestResponse> handleIllegalArgument(final WebRequest request, final IllegalArgumentException exception) {
        String message = getMessage(exception.getMessage());
        List<RestResponse.FieldError> fieldErrors = Collections.emptyList();

        return handleInvalidParamException(request, HttpStatus.BAD_REQUEST, exception, message, fieldErrors);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<RestResponse> handleIllegalState(final WebRequest request, final IllegalStateException exception) {
        String message = getMessage(exception.getMessage());
        List<RestResponse.FieldError> fieldErrors = Collections.emptyList();

        return handleInvalidParamException(request, HttpStatus.INTERNAL_SERVER_ERROR, exception, message, fieldErrors);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException exception,
            final HttpHeaders headers,
            final HttpStatusCode status, final WebRequest request) {
        List<RestResponse.FieldError> fieldErrors = new ArrayList<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.add(new RestResponse.FieldError(error.getField(), error.getDefaultMessage()))
        );

        return handleInvalidParamException(request, HttpStatus.BAD_REQUEST, exception,
                getMessage(MessageConstants.INVALID_FIELDS), fieldErrors);
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> handleInvalidParamException(final WebRequest request, final HttpStatus status,
            final Exception exception, final String message,
            final List<RestResponse.FieldError> fieldErrors) {
        return (ResponseEntity<T>) RestResponse.entity(
                status,
                exception,
                message,
                fieldErrors,
                getPath(request)
        );
    }

    private String getPath(final WebRequest request) {
        return request.getDescription(false).substring(4);
    }

    private String getMessage(final String message) {
        if (StringUtils.startsWithIgnoreCase(message, "br.com.systemrpg") && messageSource != null) {
            try {
                return messageSource.getMessage(message, null, LocaleContextHolder.getLocale());
            } catch (Exception e) {
                return message;
            }
        }

        return message;
    }
}
