package br.com.systemrpg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de grupos de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupCreateRequest {

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

    @NotBlank(message = "{validation.shortDescription.required}")
    @Size(min = 3, max = 100, message = "{validation.shortDescription.size}")
    private String shortDescription;

    @NotNull(message = "{validation.visibility.required}")
    private String visibility; // PUBLIC, FRIENDS, PRIVATE

    @NotNull(message = "{validation.accessRule.required}")
    private String accessRule; // FREE, FRIENDS, APPROVAL

    @NotNull(message = "{validation.modality.required}")
    private String modality; // ONLINE, PRESENCIAL

    private Integer minPlayers;

    private Integer maxPlayers;

    @Size(max = 100, message = "{validation.country.size}")
    private String country;

    @Size(max = 100, message = "{validation.state.size}")
    private String state;

    @Size(max = 100, message = "{validation.city.size}")
    private String city;

    @Size(max = 500, message = "{validation.themesContent.size}")
    private String themesContent;

    @Size(max = 500, message = "{validation.punctualityAttendance.size}")
    private String punctualityAttendance;

    @Size(max = 500, message = "{validation.houseRules.size}")
    private String houseRules;

    @Size(max = 500, message = "{validation.behavioralExpectations.size}")
    private String behavioralExpectations;
}