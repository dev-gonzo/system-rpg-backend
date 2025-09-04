package br.com.systemrpg.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para resposta de verificação de disponibilidade.
 */
@Schema(description = "Resposta de verificação de disponibilidade")
public class AvailabilityResponse {

    @Schema(description = "Indica se o valor está disponível", example = "true")
    private boolean available;

    public AvailabilityResponse() {
    }

    public AvailabilityResponse(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
