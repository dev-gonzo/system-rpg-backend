package br.com.systemrpg.backend.controller;

import br.com.systemrpg.backend.domain.entity.Adventure;
import br.com.systemrpg.backend.domain.entity.AdventureNote;
import br.com.systemrpg.backend.dto.request.AdventureNoteCreateRequest;
import br.com.systemrpg.backend.dto.request.AdventureNoteUpdateRequest;
import br.com.systemrpg.backend.dto.response.AdventureNoteResponse;
import br.com.systemrpg.backend.dto.response.ResponseApi;
import br.com.systemrpg.backend.mapper.AdventureNoteMapper;
import br.com.systemrpg.backend.service.AdventureNoteService;
import br.com.systemrpg.backend.service.AdventureService;
import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/adventures")
@RequiredArgsConstructor
@Validated
@Tag(name = "Adventure Notes", description = "CRUD de notas de aventuras")
@SecurityRequirement(name = "bearerAuth")
public class AdventureNoteController {

    private final AdventureNoteService adventureNoteService;
    private final AdventureService adventureService;
    private final AdventureNoteMapper adventureNoteMapper;
    private final MessageUtil messageUtil;

    @PostMapping("/{adventureId}/notes")
    @PreAuthorize("@adventureService.canManageAdventure(#p0, authentication.name)")
    @Operation(summary = "Criar nota", description = "Cria uma nota para uma aventura (apenas MASTER)")
    @ApiResponse(responseCode = "201", description = "Nota criada com sucesso")
    public ResponseEntity<ResponseApi<AdventureNoteResponse>> create(
            @Parameter(description = "ID da aventura") @PathVariable UUID adventureId,
            @Parameter(description = "Dados da nota") @Valid @RequestBody AdventureNoteCreateRequest request,
            HttpServletRequest httpRequest) {

        UUID createdByUserId = (UUID) httpRequest.getAttribute("userId");
        Adventure adventure = adventureService.findById(adventureId);
        AdventureNote note = adventureNoteService.createNote(adventure, createdByUserId, request.getTitle(), request.getContent());
        AdventureNoteResponse response = adventureNoteMapper.toResponse(note);
        String message = messageUtil.getMessage("controller.adventurenote.created.success");
        return ResponseUtil.createdWithSuccess(response, message);
    }

    @GetMapping("/{adventureId}/notes")
    @PreAuthorize("@adventureService.canViewAdventure(#p0, authentication.name)")
    @Operation(summary = "Listar notas da aventura (paginado)", description = "Lista notas por aventura com paginação")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<ResponseApi<List<AdventureNoteResponse>>> listByAdventure(
            @Parameter(description = "ID da aventura") @PathVariable UUID adventureId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AdventureNote> page = adventureNoteService.listByAdventureId(adventureId, pageable);
        List<AdventureNoteResponse> responses = page.getContent().stream()
                .map(adventureNoteMapper::toResponse)
                .toList();
        String message = messageUtil.getMessage("controller.adventurenote.list.success");
        return ResponseUtil.okWithSuccess(responses, message);
    }

    @GetMapping("/notes/{id}")
    @PreAuthorize("@adventureNoteService.canViewNote(#p0, authentication.name)")
    @Operation(summary = "Buscar nota por ID", description = "Retorna detalhes de uma nota")
    @ApiResponse(responseCode = "200", description = "Nota encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    public ResponseEntity<ResponseApi<AdventureNoteResponse>> findById(
            @Parameter(description = "ID da nota") @PathVariable UUID id) {

        AdventureNote note = adventureNoteService.findById(id);
        AdventureNoteResponse response = adventureNoteMapper.toResponse(note);
        String message = messageUtil.getMessage("controller.adventurenote.list.success");
        return ResponseUtil.okWithSuccess(response, message);
    }

    @PutMapping("/notes/{id}")
    @PreAuthorize("@adventureNoteService.canManageNote(#p0, authentication.name)")
    @Operation(summary = "Editar nota", description = "Atualiza título e conteúdo de uma nota (apenas MASTER)")
    @ApiResponse(responseCode = "200", description = "Nota atualizada com sucesso")
    public ResponseEntity<ResponseApi<AdventureNoteResponse>> update(
            @Parameter(description = "ID da nota") @PathVariable UUID id,
            @Parameter(description = "Dados para atualização") @Valid @RequestBody AdventureNoteUpdateRequest request) {

        AdventureNote updated = adventureNoteService.updateNote(id, request);
        AdventureNoteResponse response = adventureNoteMapper.toResponse(updated);
        String message = messageUtil.getMessage("controller.adventurenote.updated.success");
        return ResponseUtil.okWithSuccess(response, message);
    }

    @DeleteMapping("/notes/{id}")
    @PreAuthorize("@adventureNoteService.canManageNote(#p0, authentication.name)")
    @Operation(summary = "Apagar nota", description = "Apaga (soft delete) uma nota de uma aventura (apenas MASTER)")
    @ApiResponse(responseCode = "200", description = "Nota apagada com sucesso")
    public ResponseEntity<ResponseApi<Void>> delete(
            @Parameter(description = "ID da nota") @PathVariable UUID id) {

        adventureNoteService.deleteNote(id);
        String message = messageUtil.getMessage("controller.adventurenote.deleted.success");
        return ResponseUtil.okWithSuccess(null, message);
    }
}