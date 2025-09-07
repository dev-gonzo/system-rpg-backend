package br.com.systemrpg.backend.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.response.RoleResponse;
import br.com.systemrpg.backend.dto.UserCreateRequest;
import br.com.systemrpg.backend.dto.response.UserResponse;
import br.com.systemrpg.backend.dto.UserUpdateRequest;

/**
 * Mapper para conversão entre entidades User e DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Converte UserCreateRequest para User.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isEmailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    User toEntity(UserCreateRequest request);

    /**
     * Converte UserUpdateRequest para User.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isEmailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    User toEntity(UserUpdateRequest request);

    /**
     * Atualiza uma entidade User existente com dados do UserUpdateRequest.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isEmailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);

    /**
     * Converte User para UserResponse.
     */
    @Mapping(target = "roles", source = "roles")
    UserResponse toResponse(User user);

    /**
     * Converte Role para RoleResponse.
     */
    RoleResponse toRoleResponse(Role role);

    /**
     * Converte Set<Role> para Set<RoleResponse>.
     */
    default Set<RoleResponse> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return java.util.Collections.emptySet();
        }
        return roles.stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toSet());
    }
}
