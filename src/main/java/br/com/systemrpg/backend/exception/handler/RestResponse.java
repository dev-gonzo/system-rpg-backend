package br.com.systemrpg.backend.exception.handler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe de resposta padronizada para APIs REST.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class RestResponse {

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    private String type;

    private int status;

    private String error;

    private String message;

    @JsonInclude(value = Include.NON_NULL)
    private List<FieldError> fieldErrors;

    private String detail;

    private String path;

    private String help;

    // Setters manuais para resolver problemas do Lombok
    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    /**
     * Método utilitário para criar ResponseEntity com RestResponse.
     */
    public static ResponseEntity<RestResponse> entity(HttpStatus status, Exception exception, String message, List<FieldError> fieldErrors, String path) {
        RestResponse response = new RestResponse();
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setType(exception.getClass().getSimpleName());
        response.setMessage(message);
        response.setFieldErrors(fieldErrors);
        response.setPath(path);
                
        return ResponseEntity.status(status).headers(HttpHeaders.EMPTY).body(response);
    }

    @Getter
    @AllArgsConstructor
    public static class FieldError {

        private String iid;

        private String entity;

        private String field;

        private String error;

        public FieldError(final String fieldName, final String errorMessage) {
            this.field = fieldName;
            this.error = errorMessage;
        }
    }
}
