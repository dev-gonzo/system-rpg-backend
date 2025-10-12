package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.domain.entity.Adventure;
import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.dto.request.AdventureCreateRequest;
import br.com.systemrpg.backend.dto.response.AdventureResponse;
import org.springframework.stereotype.Component;

@Component
public class AdventureMapper {

    public Adventure toEntity(AdventureCreateRequest request, GameGroup gameGroup, java.util.UUID createdBy) {
        return Adventure.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .gameGroup(gameGroup)
                .createdBy(createdBy)
                .isActive(true)
                .build();
    }

    public AdventureResponse toResponse(Adventure adventure) {
        AdventureResponse response = new AdventureResponse();
        response.setId(adventure.getId());
        response.setTitle(adventure.getTitle());
        response.setDescription(adventure.getDescription());
        response.setIsActive(adventure.getIsActive());
        response.setGameGroupId(adventure.getGameGroup() != null ? adventure.getGameGroup().getId() : null);
        response.setCreatedAt(adventure.getCreatedAt());
        return response;
    }
}