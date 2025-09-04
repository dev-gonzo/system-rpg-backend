package br.com.systemrpg.backend.dto.response;

import lombok.Getter;

@Getter
public class SuccessResponse<T> {
    private final String message;
    private final long timestamp;
    private final T data;

    public SuccessResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.data = null;
    }

    public SuccessResponse(String message, T data) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.data = data;
    }
}
