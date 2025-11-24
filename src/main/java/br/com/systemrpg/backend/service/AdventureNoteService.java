package br.com.systemrpg.backend.service;

import br.com.systemrpg.backend.domain.entity.Adventure;
import br.com.systemrpg.backend.domain.entity.AdventureNote;
import br.com.systemrpg.backend.dto.request.AdventureNoteUpdateRequest;
import br.com.systemrpg.backend.repository.AdventureNoteRepository;
import br.com.systemrpg.backend.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdventureNoteService {

    private final AdventureNoteRepository adventureNoteRepository;
    private final AdventureService adventureService;
    private final MessageUtil messageUtil;

    public AdventureNoteService(AdventureNoteRepository adventureNoteRepository,
                                AdventureService adventureService,
                                MessageUtil messageUtil) {
        this.adventureNoteRepository = adventureNoteRepository;
        this.adventureService = adventureService;
        this.messageUtil = messageUtil;
    }

    @Transactional(readOnly = true)
    public AdventureNote findById(UUID id) {
        return adventureNoteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.getMessage("service.adventurenote.not.found")));
    }

    @Transactional
    public AdventureNote createNote(Adventure adventure, UUID createdBy, String title, String content) {
        AdventureNote note = AdventureNote.builder()
                .adventure(adventure)
                .createdBy(createdBy)
                .title(title)
                .content(content)
                .isActive(true)
                .build();
        return adventureNoteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public List<AdventureNote> listByAdventureId(UUID adventureId) {
        return adventureNoteRepository.findByAdventure_IdAndDeletedAtIsNullOrderByCreatedAtDesc(adventureId);
    }

    @Transactional(readOnly = true)
    public Page<AdventureNote> listByAdventureId(UUID adventureId, Pageable pageable) {
        return adventureNoteRepository.findByAdventure_IdAndDeletedAtIsNull(adventureId, pageable);
    }

    @Transactional
    public AdventureNote updateNote(UUID id, AdventureNoteUpdateRequest request) {
        AdventureNote note = findById(id);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setUpdatedAt(LocalDateTime.now());
        return adventureNoteRepository.save(note);
    }

    @Transactional
    public void deleteNote(UUID id) {
        AdventureNote note = findById(id);
        note.setIsActive(false);
        note.setDeletedAt(LocalDateTime.now());
        adventureNoteRepository.save(note);
    }

    // Authorization helpers for SpEL
    @Transactional(readOnly = true)
    public boolean canManageNote(UUID noteId, String username) {
        AdventureNote note = adventureNoteRepository.findByIdAndDeletedAtIsNull(noteId).orElse(null);
        if (note == null || note.getAdventure() == null) {
            return false;
        }
        return adventureService.canManageAdventure(note.getAdventure().getId(), username);
    }

    @Transactional(readOnly = true)
    public boolean canViewNote(UUID noteId, String username) {
        AdventureNote note = adventureNoteRepository.findByIdAndDeletedAtIsNull(noteId).orElse(null);
        if (note == null || note.getAdventure() == null) {
            return false;
        }
        return adventureService.canViewAdventure(note.getAdventure().getId(), username);
    }
}