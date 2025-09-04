package br.com.systemrpg.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.systemrpg.backend.domain.entity.User;

/**
 * Repositório para operações de acesso a dados da entidade User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Busca um usuário pelo username (case insensitive).
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Busca um usuário pelo username ou email (case insensitive).
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.username) = LOWER(:usernameOrEmail) OR LOWER(u.email) = LOWER(:usernameOrEmail)")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Verifica se existe um usuário com o username informado (case insensitive), excluindo usuários deletados.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.deletedAt IS NULL AND LOWER(u.username) = LOWER(:username)")
    boolean existsByUsername(@Param("username") String username);

    /**
     * Verifica se existe um usuário com o email informado (case insensitive), excluindo usuários deletados.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.deletedAt IS NULL AND LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Verifica se existe um usuário com o username informado, excluindo um ID específico (case insensitive), excluindo usuários deletados.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.deletedAt IS NULL AND LOWER(u.username) = LOWER(:username) AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") UUID id);

    /**
     * Verifica se existe um usuário com o email informado, excluindo um ID específico (case insensitive), excluindo usuários deletados.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.deletedAt IS NULL AND LOWER(u.email) = LOWER(:email) AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);

    /**
     * Busca usuários por nome ou sobrenome (case insensitive).
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Busca usuários por username, nome, sobrenome ou email (case insensitive), excluindo usuários deletados.
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findByUsernameOrFirstNameOrLastNameOrEmailContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);



    /**
     * Lista todos os usuários com paginação, incluindo roles, excluindo usuários deletados.
     */
    @EntityGraph(attributePaths = {"roles"})
    Page<User> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Busca usuários ativos ordenados por data de criação (mais recentes primeiro), excluindo usuários deletados.
     */
    @EntityGraph(attributePaths = {"roles"})
    Page<User> findByIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
}
