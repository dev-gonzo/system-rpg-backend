package br.com.systemrpg.backend.dto.request;

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

    private String campaignName;

    private String description;

    private String gameSystem;

    private String settingWorld;

    private String shortDescription;

    private String visibility; // PUBLIC, FRIENDS, PRIVATE

    private String accessRule; // FREE, FRIENDS, APPROVAL

    private String modality; // ONLINE, PRESENCIAL

    private Integer minPlayers;

    private Integer maxPlayers;

    private String country;

    private String state;

    private String city;

    private String themesContent;

    private String punctualityAttendance;

    private String houseRules;

    private String behavioralExpectations;
}