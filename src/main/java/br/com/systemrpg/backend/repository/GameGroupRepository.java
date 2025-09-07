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

import br.com.systemrpg.backend.domain.entity.GameGroup;

/**
 * Repositório para operações de acesso a dados da entidade GameGroup.
 */
@Repository
public interface GameGroupRepository extends JpaRepository<GameGroup, UUID> {

    /**
     * Busca um grupo de jogo por ID, incluindo participantes, excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    @Query("SELECT g FROM GameGroup g WHERE g.id = :id AND g.deletedAt IS NULL")
    Optional<GameGroup> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    /**
     * Lista todos os grupos de jogo com paginação, excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    Page<GameGroup> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Lista grupos de jogo ativos com paginação, excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    Page<GameGroup> findByIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Busca grupos de jogo por nome da campanha (case insensitive), excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    @Query("SELECT g FROM GameGroup g WHERE g.deletedAt IS NULL AND LOWER(g.campaignName) LIKE LOWER(CONCAT('%', :campaignName, '%')) ORDER BY g.createdAt DESC")
    Page<GameGroup> findByCampaignNameContainingIgnoreCaseAndDeletedAtIsNull(@Param("campaignName") String campaignName, Pageable pageable);

    /**
     * Busca grupos de jogo por sistema de jogo (case insensitive), excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    @Query("SELECT g FROM GameGroup g WHERE g.deletedAt IS NULL AND LOWER(g.gameSystem) LIKE LOWER(CONCAT('%', :gameSystem, '%')) ORDER BY g.createdAt DESC")
    Page<GameGroup> findByGameSystemContainingIgnoreCaseAndDeletedAtIsNull(@Param("gameSystem") String gameSystem, Pageable pageable);

    /**
     * Busca grupos de jogo por mundo de ambientação (case insensitive), excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    @Query("SELECT g FROM GameGroup g WHERE g.deletedAt IS NULL AND LOWER(g.settingWorld) LIKE LOWER(CONCAT('%', :settingWorld, '%')) ORDER BY g.createdAt DESC")
    Page<GameGroup> findBySettingWorldContainingIgnoreCaseAndDeletedAtIsNull(@Param("settingWorld") String settingWorld, Pageable pageable);

    /**
     * Busca grupos de jogo por múltiplos filtros (case insensitive), excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    @Query("SELECT g FROM GameGroup g WHERE g.deletedAt IS NULL " +
           "AND (:campaignName IS NULL OR LOWER(g.campaignName) LIKE LOWER(CONCAT('%', :campaignName, '%'))) " +
           "AND (:gameSystem IS NULL OR LOWER(g.gameSystem) LIKE LOWER(CONCAT('%', :gameSystem, '%'))) " +
           "AND (:settingWorld IS NULL OR LOWER(g.settingWorld) LIKE LOWER(CONCAT('%', :settingWorld, '%'))) " +
           "ORDER BY g.createdAt DESC")
    Page<GameGroup> findByFiltersAndDeletedAtIsNull(
        @Param("campaignName") String campaignName,
        @Param("gameSystem") String gameSystem,
        @Param("settingWorld") String settingWorld,
        Pageable pageable
    );

    /**
     * Busca grupos de jogo por uma lista de IDs, excluindo grupos deletados.
     */
    @EntityGraph(attributePaths = {"participants"})
    @Query("SELECT g FROM GameGroup g WHERE g.id IN :ids AND g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    Page<GameGroup> findByIdsAndDeletedAtIsNull(@Param("ids") List<UUID> ids, Pageable pageable);

    /**
     * Verifica se existe um grupo de jogo com o nome da campanha informado (case insensitive), excluindo grupos deletados.
     */
    @Query("SELECT COUNT(g) > 0 FROM GameGroup g WHERE g.deletedAt IS NULL AND LOWER(g.campaignName) = LOWER(:campaignName)")
    boolean existsByCampaignNameIgnoreCaseAndDeletedAtIsNull(@Param("campaignName") String campaignName);

    /**
     * Verifica se existe um grupo de jogo com o nome da campanha informado, excluindo um ID específico (case insensitive), excluindo grupos deletados.
     */
    @Query("SELECT COUNT(g) > 0 FROM GameGroup g WHERE g.deletedAt IS NULL AND g.id != :id AND LOWER(g.campaignName) = LOWER(:campaignName)")
    boolean existsByCampaignNameIgnoreCaseAndIdNotAndDeletedAtIsNull(@Param("campaignName") String campaignName, @Param("id") UUID id);
}