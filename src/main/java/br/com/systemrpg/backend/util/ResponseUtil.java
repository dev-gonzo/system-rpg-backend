package br.com.systemrpg.backend.util;

import br.com.systemrpg.backend.dto.response.ApiResponse;

import br.com.systemrpg.backend.dto.response.ResponseApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utilitário para centralizar a criação de ResponseEntity.
 * Reduz a duplicação de código ao criar respostas HTTP padronizadas.
 */
public class ResponseUtil {

    /**
     * Construtor privado para prevenir instanciação.
     */
    private ResponseUtil() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não deve ser instanciada");
    }

    /**
     * Cria uma resposta de sucesso (200 OK).
     *
     * @param data os dados da resposta
     * @param <T> tipo dos dados
     * @return ResponseEntity com status 200
     */
    public static <T> ResponseEntity<T> ok(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Cria uma resposta de sucesso com ApiResponse.
     *
     * @param data os dados da resposta
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ResponseEntity com status 200
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Cria uma resposta de sucesso sem dados.
     *
     * @param message mensagem de sucesso
     * @return ResponseEntity com status 200
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Cria uma resposta de criação (201 Created).
     *
     * @param data os dados criados
     * @param <T> tipo dos dados
     * @return ResponseEntity com status 201
     */
    public static <T> ResponseEntity<T> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    /**
     * Cria uma resposta de criação com ApiResponse.
     *
     * @param data os dados criados
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ResponseEntity com status 201
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data, message));
    }

    /**
     * Cria uma resposta de requisição inválida (400 Bad Request).
     *
     * @param message mensagem de erro
     * @return ResponseEntity com status 400
     */
    public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    /**
     * Cria uma resposta de requisição inválida com ResponseApi.
     *
     * @param message mensagem de erro
     * @param details detalhes do erro
     * @return ResponseEntity com status 400
     */
    public static ResponseEntity<ResponseApi<String>> badRequest(String message, String details) {
        return ResponseEntity.badRequest().body(ResponseApi.error(message, details));
    }

    /**
     * Cria uma resposta de não autorizado (401 Unauthorized).
     *
     * @param message mensagem de erro
     * @return ResponseEntity com status 401
     */
    public static ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message));
    }

    /**
     * Cria uma resposta de não autorizado com ResponseApi.
     *
     * @param message mensagem de erro
     * @param details detalhes do erro
     * @return ResponseEntity com status 401
     */
    public static ResponseEntity<ResponseApi<String>> unauthorized(String message, String details) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseApi.error(message, details));
    }

    /**
     * Cria uma resposta de acesso negado (403 Forbidden).
     *
     * @param message mensagem de erro
     * @return ResponseEntity com status 403
     */
    public static ResponseEntity<ApiResponse<Void>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message));
    }

    /**
     * Cria uma resposta de não encontrado (404 Not Found).
     *
     * @param message mensagem de erro
     * @return ResponseEntity com status 404
     */
    public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message));
    }

    /**
     * Cria uma resposta de conflito (409 Conflict).
     *
     * @param message mensagem de erro
     * @return ResponseEntity com status 409
     */
    public static ResponseEntity<ApiResponse<Void>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(message));
    }

    /**
     * Cria uma resposta de erro interno do servidor (500 Internal Server Error).
     *
     * @param message mensagem de erro
     * @return ResponseEntity com status 500
     */
    public static ResponseEntity<ApiResponse<Void>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message));
    }

    /**
     * Cria uma resposta de erro interno do servidor com ResponseApi.
     *
     * @param message mensagem de erro
     * @param details detalhes do erro
     * @return ResponseEntity com status 500
     */
    public static ResponseEntity<ResponseApi<String>> internalServerError(String message, String details) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseApi.error(message, details));
    }

    /**
     * Cria uma resposta com status customizado.
     *
     * @param status o status HTTP
     * @param data os dados da resposta
     * @param <T> tipo dos dados
     * @return ResponseEntity com status customizado
     */
    public static <T> ResponseEntity<T> status(HttpStatus status, T data) {
        return ResponseEntity.status(status).body(data);
    }

    /**
     * Cria uma resposta com status customizado e ApiResponse.
     *
     * @param status o status HTTP
     * @param data os dados da resposta
     * @param message mensagem
     * @param <T> tipo dos dados
     * @return ResponseEntity com status customizado
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(HttpStatus status, T data, String message) {
        ApiResponse<T> response = status.is2xxSuccessful() ? 
            ApiResponse.success(data, message) : 
            ApiResponse.error(message, data);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Cria uma resposta sem conteúdo (204 No Content).
     *
     * @return ResponseEntity com status 204
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Cria uma resposta de aceito (202 Accepted).
     *
     * @param message mensagem de confirmação
     * @return ResponseEntity com status 202
     */
    public static ResponseEntity<ApiResponse<Void>> accepted(String message) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(message));
    }

    /**
     * Cria uma ResponseApi com dados e mensagem.
     *
     * @param data os dados da resposta
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ResponseApi
     */
    public static <T> ResponseApi<T> successResponse(T data, String message) {
        return ResponseApi.success(message, data);
    }

    /**
     * Cria uma ResponseApi apenas com mensagem.
     *
     * @param message mensagem de sucesso
     * @return ResponseApi
     */
    public static ResponseApi<Void> successResponse(String message) {
        return ResponseApi.success(message);
    }

    /**
     * Cria uma resposta de sucesso (200 OK) com ResponseApi.
     *
     * @param data os dados da resposta
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ResponseEntity com status 200
     */
    public static <T> ResponseEntity<ResponseApi<T>> okWithSuccess(T data, String message) {
        return ResponseEntity.ok(successResponse(data, message));
    }

    /**
     * Cria uma resposta de criação (201 Created) com ResponseApi.
     *
     * @param data os dados criados
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ResponseEntity com status 201
     */
    public static <T> ResponseEntity<ResponseApi<T>> createdWithSuccess(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse(data, message));
    }
}
