package br.com.systemrpg.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;

/**
 * Repositório para operações de acesso a dados da entidade GameGroupParticipant.
 */
@Repository
public interface GameGroupParticipantRepository extends JpaRepository<GameGroupParticipant, UUID> {

    /**
     * Busca um participante por ID, incluindo grupo e usuário, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "user"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<GameGroupParticipant> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    /**
     * Lista todos os participantes de um grupo específico, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.gameGroup.id = :gameGroupId AND p.deletedAt IS NULL ORDER BY p.createdAt ASC")
    List<GameGroupParticipant> findByGameGroupIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId);

    /**
     * Lista todos os participantes de um grupo específico com paginação, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.gameGroup.id = :gameGroupId AND p.deletedAt IS NULL ORDER BY p.createdAt ASC")
    Page<GameGroupParticipant> findByGameGroupIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId, Pageable pageable);

    /**
     * Lista participantes ativos de um grupo específico, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.gameGroup.id = :gameGroupId AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.createdAt ASC")
    List<GameGroupParticipant> findByGameGroupIdAndIsActiveTrueAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId);

    /**
     * Lista todos os grupos que um usuário participa, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<GameGroupParticipant> findByUserIdAndDeletedAtIsNull(@Param("userId") UUID userId);

    /**
     * Lista grupos ativos que um usuário participa, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.user.id = :userId AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<GameGroupParticipant> findByUserIdAndIsActiveTrueAndDeletedAtIsNull(@Param("userId") UUID userId);

    /**
     * Busca um participante específico por grupo e usuário, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "user"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.gameGroup.id = :gameGroupId AND p.user.id = :userId AND p.deletedAt IS NULL")
    Optional<GameGroupParticipant> findByGameGroupIdAndUserIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId, @Param("userId") UUID userId);

    /**
     * Verifica se um usuário já participa de um grupo específico, excluindo participantes deletados.
     */
    @Query("SELECT COUNT(p) > 0 FROM GameGroupParticipant p WHERE p.gameGroup.id = :gameGroupId AND p.user.id = :userId AND p.deletedAt IS NULL")
    boolean existsByGameGroupIdAndUserIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId, @Param("userId") UUID userId);

    /**
     * Busca o master de um grupo específico, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM GameGroupParticipant p WHERE p.gameGroup.id = :gameGroupId AND p.role = 0 AND p.deletedAt IS NULL")
    Optional<GameGroupParticipant> findMasterByGameGroupIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId);

    /**
     * Conta o número de participantes ativos em um grupo, excluindo participantes deletados.
     */
    @Query("SELECT COUNT(p) FROM GameGroupParticipant p WHERE p.gameGroup.id = :gameGroupId AND p.isActive = true AND p.deletedAt IS NULL")
    long countActiveParticipantsByGameGroupId(@Param("gameGroupId") UUID gameGroupId);

    /**
     * Lista todos os participantes com paginação, excluindo participantes deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "user"})
    Page<GameGroupParticipant> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
}