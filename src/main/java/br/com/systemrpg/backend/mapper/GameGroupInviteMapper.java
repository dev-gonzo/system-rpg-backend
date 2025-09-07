package br.com.systemrpg.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import br.com.systemrpg.backend.domain.entity.GameGroupInvite;
import br.com.systemrpg.backend.dto.response.GameGroupInviteResponse;

/**
 * Mapper para conversão entre entidades GameGroupInvite e DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {GameGroupMapper.class, UserMapper.class})
public interface GameGroupInviteMapper {

    /**
     * Converte GameGroupInvite para GameGroupInviteResponse.
     */
    @Mapping(target = "role", expression = "java(invite.getRole() != null ? br.com.systemrpg.backend.domain.entity.GameGroupInvite.InviteRole.fromValue(invite.getRole()).name() : null)")
    GameGroupInviteResponse toResponse(GameGroupInvite invite);
}