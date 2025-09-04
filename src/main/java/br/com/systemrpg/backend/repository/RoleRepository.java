package br.com.systemrpg.backend.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.systemrpg.backend.domain.entity.Role;

/**
 * Repositório para operações de acesso a dados da entidade Role.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Busca uma role pelo nome.
     */
    Optional<Role> findByName(String name);

    /**
     * Busca roles ativas por nomes.
     */
    @Query("SELECT r FROM Role r WHERE r.name IN :names AND r.isActive = true")
    Set<Role> findByNameInAndIsActiveTrue(@Param("names") Set<String> names);
}
