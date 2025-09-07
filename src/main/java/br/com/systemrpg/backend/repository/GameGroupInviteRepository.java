package br.com.systemrpg.backend.repository;

import java.time.LocalDateTime;
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

import br.com.systemrpg.backend.domain.entity.GameGroupInvite;

/**
 * Repositório para operações de acesso a dados da entidade GameGroupInvite.
 */
@Repository
public interface GameGroupInviteRepository extends JpaRepository<GameGroupInvite, UUID> {

    /**
     * Busca um convite por código, incluindo grupo e usuários, excluindo convites deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "createdByUser", "usedByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.inviteCode = :inviteCode AND i.deletedAt IS NULL")
    Optional<GameGroupInvite> findByInviteCodeAndDeletedAtIsNull(@Param("inviteCode") String inviteCode);

    /**
     * Busca um convite por ID, incluindo grupo e usuários, excluindo convites deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "createdByUser", "usedByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.id = :id AND i.deletedAt IS NULL")
    Optional<GameGroupInvite> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    /**
     * Lista todos os convites de um grupo específico, excluindo convites deletados.
     */
    @EntityGraph(attributePaths = {"createdByUser", "usedByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.gameGroup.id = :gameGroupId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<GameGroupInvite> findByGameGroupIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId);

    /**
     * Lista convites de um grupo com paginação, excluindo convites deletados.
     */
    @EntityGraph(attributePaths = {"createdByUser", "usedByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.gameGroup.id = :gameGroupId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<GameGroupInvite> findByGameGroupIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId, Pageable pageable);

    /**
     * Lista convites válidos (não usados e não expirados) de um grupo.
     */
    @EntityGraph(attributePaths = {"createdByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.gameGroup.id = :gameGroupId " +
           "AND i.isUsed = false " +
           "AND (i.expiresAt IS NULL OR i.expiresAt > :now) " +
           "AND i.deletedAt IS NULL " +
           "ORDER BY i.createdAt DESC")
    List<GameGroupInvite> findValidInvitesByGameGroupId(@Param("gameGroupId") UUID gameGroupId, @Param("now") LocalDateTime now);

    /**
     * Lista convites criados por um usuário específico, excluindo convites deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "usedByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.createdByUser.id = :userId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<GameGroupInvite> findByCreatedByUserIdAndDeletedAtIsNull(@Param("userId") UUID userId);

    /**
     * Lista convites criados por um usuário com paginação, excluindo convites deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "usedByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.createdByUser.id = :userId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<GameGroupInvite> findByCreatedByUserIdAndDeletedAtIsNull(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Lista convites usados por um usuário específico, excluindo convites deletados.
     */
    @EntityGraph(attributePaths = {"gameGroup", "createdByUser"})
    @Query("SELECT i FROM GameGroupInvite i WHERE i.usedByUser.id = :userId AND i.deletedAt IS NULL ORDER BY i.usedAt DESC")
    List<GameGroupInvite> findByUsedByUserIdAndDeletedAtIsNull(@Param("userId") UUID userId);

    /**
     * Conta convites válidos (não usados e não expirados) de um grupo.
     */
    @Query("SELECT COUNT(i) FROM GameGroupInvite i WHERE i.gameGroup.id = :gameGroupId " +
           "AND i.isUsed = false " +
           "AND (i.expiresAt IS NULL OR i.expiresAt > :now) " +
           "AND i.deletedAt IS NULL")
    long countValidInvitesByGameGroupId(@Param("gameGroupId") UUID gameGroupId, @Param("now") LocalDateTime now);

    /**
     * Conta convites criados por um usuário em um grupo específico.
     */
    @Query("SELECT COUNT(i) FROM GameGroupInvite i WHERE i.gameGroup.id = :gameGroupId " +
           "AND i.createdByUser.id = :userId " +
           "AND i.deletedAt IS NULL")
    long countByGameGroupIdAndCreatedByUserIdAndDeletedAtIsNull(@Param("gameGroupId") UUID gameGroupId, @Param("userId") UUID userId);

    /**
     * Verifica se existe um convite válido com o código especificado.
     */
    @Query("SELECT COUNT(i) > 0 FROM GameGroupInvite i WHERE i.inviteCode = :inviteCode " +
           "AND i.isUsed = false " +
           "AND (i.expiresAt IS NULL OR i.expiresAt > :now) " +
           "AND i.deletedAt IS NULL")
    boolean existsValidInviteByCode(@Param("inviteCode") String inviteCode, @Param("now") LocalDateTime now);

    /**
     * Remove convites expirados (soft delete).
     */
    @Query("UPDATE GameGroupInvite i SET i.deletedAt = :now " +
           "WHERE i.expiresAt < :now AND i.deletedAt IS NULL")
    int deleteExpiredInvites(@Param("now") LocalDateTime now);
}