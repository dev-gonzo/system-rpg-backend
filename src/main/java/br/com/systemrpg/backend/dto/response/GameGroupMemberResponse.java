package br.com.systemrpg.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de membros de grupos de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupMemberResponse {

    private UUID id;
    
    private UUID participantId;

    private String username;
    
    private String role;

    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Getter manual para resolver problemas do Lombok
    public Boolean getIsActive() {
        return isActive;
    }
}