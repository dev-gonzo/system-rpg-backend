package br.com.systemrpg.backend.dto.hateoas;

import br.com.systemrpg.backend.dto.response.GameGroupMemberResponse;
import br.com.systemrpg.backend.dto.response.UserResponse;
import br.com.systemrpg.backend.hateoas.HateoasResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO HATEOAS para resposta de grupos de jogo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameGroupHateoasResponse extends HateoasResponse {
    
    private UUID id;
    
    private String campaignName;
    
    private String description;
    
    private String shortDescription;
    
    private String gameSystem;
    
    private String settingWorld;
    
    private String visibility; // PUBLIC, FRIENDS, PRIVATE
    
    private String accessRule; // FREE, FRIENDS, APPROVAL
    
    private String modality; // ONLINE, PRESENCIAL
    
    private Integer maxPlayers;
    
    private Integer currentParticipants;
    
    private List<GameGroupMemberResponse> participants;
    
    private String themesContent;
    
    private String punctualityAttendance;
    
    private String houseRules;
    
    private String behavioralExpectations;
    
    private String location;
    
    private String rules;
    
    private String notes;
    
    private Boolean isActive;
    
    private UserResponse createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    // Getter manual para resolver problemas do Lombok
    public UUID getId() {
        return id;
    }
}