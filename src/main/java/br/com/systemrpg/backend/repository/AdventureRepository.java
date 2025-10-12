package br.com.systemrpg.backend.repository;

import br.com.systemrpg.backend.domain.entity.Adventure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdventureRepository extends JpaRepository<Adventure, UUID> {
    List<Adventure> findByGameGroup_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID gameGroupId);
    Page<Adventure> findByGameGroup_IdAndDeletedAtIsNull(UUID gameGroupId, Pageable pageable);
    Optional<Adventure> findByIdAndDeletedAtIsNull(UUID id);
}