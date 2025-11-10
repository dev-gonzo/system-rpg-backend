package br.com.systemrpg.backend.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO leve para adicionar participante via endpoint aninhado,
 * sem exigir gameGroupId no corpo (vem do path).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupParticipantAddRequest {

    @NotNull(message = "{validation.userId.required}")
    private UUID userId;

    private String role; // MASTER, PLAYER, GUEST - opcional, padrão será PLAYER

    // Getters manuais para evitar problemas do Lombok em alguns ambientes
    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}