package br.com.systemrpg.backend.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de participantes de grupos de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupParticipantCreateRequest {

    @NotNull(message = "{validation.gameGroupId.required}")
    private UUID gameGroupId;

    @NotNull(message = "{validation.userId.required}")
    private UUID userId;

    private String role; // MASTER, PLAYER, GUEST - opcional, padrão será PLAYER
    
    // Getters manuais para resolver problemas do Lombok
    public String getRole() {
        return role;
    }
    
    public UUID getGameGroupId() {
        return gameGroupId;
    }
    
    public UUID getUserId() {
        return userId;
    }
}