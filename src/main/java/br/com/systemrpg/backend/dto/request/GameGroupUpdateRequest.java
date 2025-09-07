package br.com.systemrpg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de grupos de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupUpdateRequest {

    @NotBlank(message = "{validation.campaignName.required}")
    @Size(min = 3, max = 100, message = "{validation.campaignName.size}")
    private String campaignName;

    @Size(max = 500, message = "{validation.description.size}")
    private String description;

    @NotBlank(message = "{validation.gameSystem.required}")
    @Size(min = 2, max = 50, message = "{validation.gameSystem.size}")
    private String gameSystem;

    @Size(max = 100, message = "{validation.settingWorld.size}")
    private String settingWorld;

    @NotNull(message = "{validation.accessRule.required}")
    private Integer accessRule; // 0-free, 1-friends, 2-approval

    @NotNull(message = "{validation.modality.required}")
    private Integer modality; // 0-online, 1-presencial

    @NotNull(message = "{validation.maxParticipants.required}")
    private Integer maxParticipants;

    @Size(max = 200, message = "{validation.location.size}")
    private String location;

    @Size(max = 1000, message = "{validation.rules.size}")
    private String rules;

    @Size(max = 1000, message = "{validation.notes.size}")
    private String notes;
}