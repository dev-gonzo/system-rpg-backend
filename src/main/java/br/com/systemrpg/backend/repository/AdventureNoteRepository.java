package br.com.systemrpg.backend.repository;

import br.com.systemrpg.backend.domain.entity.AdventureNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdventureNoteRepository extends JpaRepository<AdventureNote, UUID> {

    List<AdventureNote> findByAdventure_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID adventureId);

    Page<AdventureNote> findByAdventure_IdAndDeletedAtIsNull(UUID adventureId, Pageable pageable);

    Optional<AdventureNote> findByIdAndDeletedAtIsNull(UUID id);
}