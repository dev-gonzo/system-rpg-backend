package br.com.systemrpg.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * Classe unificada para respostas da API.
 * Combina funcionalidades de SuccessResponse e ErrorResponse.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseApi<T> {
    private final String message;
    private final long timestamp;
    private final T data;
    private final String error;

    // Construtor privado para controlar a criação
    private ResponseApi(String message, T data, String error) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.data = data;
        this.error = error;
    }

    /**
     * Cria uma resposta de sucesso com mensagem e conteúdo.
     *
     * @param message mensagem de sucesso
     * @param data conteúdo da resposta
     * @param <T> tipo do conteúdo
     * @return ResponseApi de sucesso
     */
    public static <T> ResponseApi<T> success(String message, T data) {
        return new ResponseApi<>(message, data, null);
    }

    /**
     * Cria uma resposta de sucesso apenas com mensagem.
     *
     * @param message mensagem de sucesso
     * @return ResponseApi de sucesso
     */
    public static ResponseApi<Void> success(String message) {
        return new ResponseApi<>(message, null, null);
    }

    /**
     * Cria uma resposta de erro com mensagem de erro.
     *
     * @param error mensagem de erro
     * @return ResponseApi de erro
     */
    public static ResponseApi<Void> error(String error) {
        return new ResponseApi<>(null, null, error);
    }

    /**
     * Cria uma resposta de erro com mensagem e conteúdo.
     *
     * @param error mensagem de erro
     * @param data conteúdo da resposta de erro
     * @param <T> tipo do conteúdo
     * @return ResponseApi de erro
     */
    public static <T> ResponseApi<T> error(String error, T data) {
        return new ResponseApi<>(null, data, error);
    }

    /**
     * Verifica se a resposta é de sucesso.
     *
     * @return true se for sucesso, false se for erro
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Verifica se a resposta é de erro.
     *
     * @return true se for erro, false se for sucesso
     */
    public boolean isError() {
        return error != null;
    }
}