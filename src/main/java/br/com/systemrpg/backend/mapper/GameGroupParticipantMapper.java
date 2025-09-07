package br.com.systemrpg.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.dto.response.GameGroupParticipantResponse;

/**
 * Mapper para conversão entre entidades GameGroupParticipant e DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {GameGroupMapper.class, UserMapper.class})
public interface GameGroupParticipantMapper {

    /**
     * Converte GameGroupParticipant para GameGroupParticipantResponse.
     */
    @Mapping(target = "role", expression = "java(participant.getRole() != null ? br.com.systemrpg.backend.domain.entity.GameGroupParticipant.ParticipantRole.fromValue(participant.getRole()).name() : null)")
    GameGroupParticipantResponse toResponse(GameGroupParticipant participant);
}