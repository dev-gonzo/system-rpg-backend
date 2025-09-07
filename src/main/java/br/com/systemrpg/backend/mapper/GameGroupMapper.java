package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.dto.request.GameGroupCreateRequest;
import br.com.systemrpg.backend.dto.request.GameGroupUpdateRequest;
import br.com.systemrpg.backend.dto.response.GameGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para conversão entre entidades GameGroup e DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {UserMapper.class})
public interface GameGroupMapper {

    /**
     * Converte GameGroupCreateRequest para GameGroup.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "visibility", expression = "java(request.getVisibility() != null ? br.com.systemrpg.backend.domain.entity.GameGroup.Visibility.valueOf(request.getVisibility()) : null)")
    @Mapping(target = "accessRule", expression = "java(request.getAccessRule() != null ? br.com.systemrpg.backend.domain.entity.GameGroup.AccessRule.valueOf(request.getAccessRule()) : null)")
    @Mapping(target = "modality", expression = "java(request.getModality() != null ? br.com.systemrpg.backend.domain.entity.GameGroup.Modality.valueOf(request.getModality()) : null)")
    GameGroup toEntity(GameGroupCreateRequest request);
    
    /**
     * Atualiza uma entidade GameGroup existente com dados do GameGroupUpdateRequest.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "visibility", expression = "java(request.getVisibility() != null ? br.com.systemrpg.backend.domain.entity.GameGroup.Visibility.valueOf(request.getVisibility()) : null)")
    @Mapping(target = "accessRule", expression = "java(request.getAccessRule() != null ? br.com.systemrpg.backend.domain.entity.GameGroup.AccessRule.valueOf(request.getAccessRule()) : null)")
    @Mapping(target = "modality", expression = "java(request.getModality() != null ? br.com.systemrpg.backend.domain.entity.GameGroup.Modality.valueOf(request.getModality()) : null)")
    void updateEntityFromRequest(GameGroupUpdateRequest request, @MappingTarget GameGroup gameGroup);

    /**
     * Converte GameGroup para GameGroupResponse.
     */
    @Mapping(target = "visibility", expression = "java(gameGroup.getVisibility() != null ? gameGroup.getVisibility().name() : null)")
    @Mapping(target = "accessRule", expression = "java(gameGroup.getAccessRule() != null ? gameGroup.getAccessRule().name() : null)")
    @Mapping(target = "modality", expression = "java(gameGroup.getModality() != null ? gameGroup.getModality().name() : null)")
    @Mapping(target = "currentParticipants", expression = "java(gameGroup.getParticipants() != null ? (int) gameGroup.getParticipants().stream().filter(p -> p.getIsActive() && p.getDeletedAt() == null).count() : 0)")
    GameGroupResponse toResponse(GameGroup gameGroup);
}