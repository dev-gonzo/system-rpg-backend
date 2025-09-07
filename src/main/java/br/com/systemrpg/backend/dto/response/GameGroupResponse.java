package br.com.systemrpg.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de grupos de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupResponse {

    private UUID id;
    
    private String campaignName;
    
    private String description;
    
    private String gameSystem;
    
    private String settingWorld;
    
    private String accessRule; // FREE, FRIENDS, APPROVAL
    
    private String modality; // ONLINE, PRESENCIAL
    
    private Integer maxParticipants;
    
    private Integer currentParticipants;
    
    private String location;
    
    private String rules;
    
    private String notes;
    
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    // Getter manual para resolver problemas do Lombok
    public Boolean getIsActive() {
        return isActive;
    }
}