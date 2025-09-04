package br.com.systemrpg.backend.domain.enumeration;

import lombok.Getter;

/**
 * Enumeração que define os roles/papéis disponíveis no sistema de autenticação.
 * Utilizada para controle de acesso e autorização.
 */
public enum RoleEnum {

    ADMINISTRADOR("admin"),
    USUARIO("user");

    @Getter
    private final String name;

    RoleEnum(final String roleName) {
        this.name = roleName;
    }
}
