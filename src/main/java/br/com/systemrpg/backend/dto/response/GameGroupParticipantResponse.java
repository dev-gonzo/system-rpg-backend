package br.com.systemrpg.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de participantes de grupos de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupParticipantResponse {

    private UUID id;
    
    private GameGroupResponse gameGroup;
    
    private UserResponse user;
    
    private String role; // MASTER, PLAYER, GUEST
    
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;
    
    // Getters manuais para resolver problemas do Lombok
    public UUID getId() {
        return id;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
}