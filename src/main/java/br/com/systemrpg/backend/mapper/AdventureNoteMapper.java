package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.domain.entity.Adventure;
import br.com.systemrpg.backend.domain.entity.AdventureNote;
import br.com.systemrpg.backend.dto.request.AdventureNoteCreateRequest;
import br.com.systemrpg.backend.dto.response.AdventureNoteResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdventureNoteMapper {

    public AdventureNote toEntity(AdventureNoteCreateRequest request, Adventure adventure, UUID createdBy) {
        return AdventureNote.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .isActive(true)
                .createdBy(createdBy)
                .adventure(adventure)
                .build();
    }

    public AdventureNoteResponse toResponse(AdventureNote note) {
        AdventureNoteResponse response = new AdventureNoteResponse();
        response.setId(note.getId());
        response.setAdventureId(note.getAdventure() != null ? note.getAdventure().getId() : null);
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setCreatedBy(note.getCreatedBy());
        response.setIsActive(note.getIsActive());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        return response;
    }
}