package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.dto.response.GameGroupMemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para conversão entre GameGroupParticipant e GameGroupMemberResponse.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GameGroupMemberMapper {

    /**
     * Converte GameGroupParticipant para GameGroupMemberResponse.
     * Mapeia apenas participantes ativos (isActive = true e deletedAt = null).
     */
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "participantId", source = "id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "role", expression = "java(participant.getRole() != null ? br.com.systemrpg.backend.domain.entity.GameGroupParticipant.ParticipantRole.fromValue(participant.getRole()).name() : null)")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    GameGroupMemberResponse toResponse(GameGroupParticipant participant);
}