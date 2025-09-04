package br.com.systemrpg.backend.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.systemrpg.backend.domain.entity.TokenBlacklist;

/**
 * Repositório para gerenciamento de tokens na blacklist.
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {

    /**
     * Verifica se um token está na blacklist pelo hash.
     */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Remove todos os tokens expirados da blacklist.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

}
