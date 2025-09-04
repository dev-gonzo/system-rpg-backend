package br.com.systemrpg.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * Classe genérica para respostas da API.
 * Padroniza o formato de retorno das operações.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final long timestamp;
    private final String error;

    private ApiResponse(boolean success, String message, T data, String error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Cria uma resposta de sucesso com dados e mensagem.
     *
     * @param data os dados da resposta
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ApiResponse de sucesso
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null);
    }

    /**
     * Cria uma resposta de sucesso apenas com mensagem.
     *
     * @param message mensagem de sucesso
     * @return ApiResponse de sucesso
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    /**
     * Cria uma resposta de erro.
     *
     * @param message mensagem de erro
     * @return ApiResponse de erro
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, null, null, message);
    }

    /**
     * Cria uma resposta de erro com dados.
     *
     * @param message mensagem de erro
     * @param data dados do erro
     * @param <T> tipo dos dados
     * @return ApiResponse de erro
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, null, data, message);
    }
}
