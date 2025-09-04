package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.dto.AuthResponse;
import br.com.systemrpg.backend.dto.hateoas.AuthHateoasResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para conversão entre AuthResponse e AuthHateoasResponse.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthHateoasMapper {
    
    /**
     * Converte AuthResponse para AuthHateoasResponse.
     */
    @Mapping(target = "links", ignore = true)
    AuthHateoasResponse toHateoasResponse(AuthResponse authResponse);
    
    /**
     * Converte AuthResponse.UserInfo para AuthHateoasResponse.UserInfo.
     */
    AuthHateoasResponse.UserInfo toHateoasUserInfo(AuthResponse.UserInfo userInfo);
}
