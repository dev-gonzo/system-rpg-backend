package br.com.systemrpg.backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.dto.request.GameGroupParticipantCreateRequest;
import br.com.systemrpg.backend.dto.response.GameGroupParticipantResponse;
import br.com.systemrpg.backend.dto.response.ResponseApi;
import br.com.systemrpg.backend.hateoas.HateoasLinkBuilder;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import br.com.systemrpg.backend.hateoas.PageInfo;
import br.com.systemrpg.backend.mapper.GameGroupParticipantMapper;
import br.com.systemrpg.backend.service.GameGroupParticipantService;
import br.com.systemrpg.backend.util.ResponseUtil;
import br.com.systemrpg.backend.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável por gerenciar participantes de grupos de jogo.
 */
@RestController
@RequestMapping("/api/v1/game-group-participants")
@RequiredArgsConstructor
@Validated
@Tag(name = "Game Group Participants", description = "Operações relacionadas aos participantes de grupos de jogo")
@SecurityRequirement(name = "bearerAuth")
public class GameGroupParticipantController {

    private final GameGroupParticipantService gameGroupParticipantService;
    private final GameGroupParticipantMapper gameGroupParticipantMapper;
    private final HateoasLinkBuilder hateoasLinkBuilder;
    private final MessageUtil messageUtil;

    /**
     * Lista todos os participantes de grupos de jogo com paginação.
     */
    @GetMapping
    @Operation(summary = "Listar participantes de grupos de jogo", description = "Lista todos os participantes de grupos de jogo com paginação")
    public ResponseEntity<PagedHateoasResponse<GameGroupParticipantResponse>> listAll(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "ID do grupo de jogo para filtrar") @RequestParam(required = false) UUID gameGroupId,
            @Parameter(description = "ID do usuário para filtrar") @RequestParam(required = false) UUID userId,
            @Parameter(description = "Papel do participante para filtrar") @RequestParam(required = false) String role,
            @Parameter(description = "Status ativo para filtrar") @RequestParam(required = false) Boolean isActive) {
        
        Page<GameGroupParticipantResponse> participants = gameGroupParticipantService.findAll(
                pageable, gameGroupId, userId, role, isActive);
        
        PageInfo pageInfo = new PageInfo();
        pageInfo.setNumber(participants.getNumber());
        pageInfo.setSize(participants.getSize());
        pageInfo.setTotalElements(participants.getTotalElements());
        pageInfo.setTotalPages(participants.getTotalPages());
        pageInfo.setFirst(participants.isFirst());
        pageInfo.setLast(participants.isLast());
        pageInfo.setHasNext(participants.hasNext());
        pageInfo.setHasPrevious(participants.hasPrevious());
        pageInfo.setNumberOfElements(participants.getNumberOfElements());
        
        PagedHateoasResponse<GameGroupParticipantResponse> response = new PagedHateoasResponse<>(participants.getContent(), pageInfo);
        
        // Adicionar links HATEOAS para cada participante
        response.getContent().forEach(participant -> {
            hateoasLinkBuilder.addGameGroupParticipantLinks(response, participant.getId());
        });
        
        // Adicionar links de paginação
        String queryParams = buildQueryParams(gameGroupId, userId, role, isActive);
        hateoasLinkBuilder.addPaginationLinks(response, pageable, "/game-group-participants", queryParams);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um participante de grupo de jogo por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@gameGroupParticipantService.isParticipantOwner(#id, authentication.name)")
    @Operation(summary = "Buscar participante por ID", description = "Busca um participante de grupo de jogo pelo seu ID")
    public ResponseEntity<ResponseApi<GameGroupParticipantResponse>> findById(
            @Parameter(description = "ID do participante") @PathVariable UUID id) {
        
        GameGroupParticipantResponse participant = gameGroupParticipantService.findByIdAsResponse(id);
        
        return ResponseUtil.okWithSuccess(participant, messageUtil.getMessage("controller.gamegroupparticipant.found.success"));
    }

    /**
     * Adiciona um novo participante a um grupo de jogo.
     */
    @PostMapping
    @PreAuthorize("@gameGroupService.isGroupOwner(#request.gameGroupId, authentication.name)")
    @Operation(summary = "Adicionar participante", description = "Adiciona um novo participante a um grupo de jogo")
    public ResponseEntity<ResponseApi<GameGroupParticipantResponse>> create(
            @Valid @RequestBody GameGroupParticipantCreateRequest request) {
        
        GameGroupParticipantResponse participant = gameGroupParticipantService.create(request);
        
        return ResponseUtil.createdWithSuccess(participant, messageUtil.getMessage("controller.gamegroupparticipant.created.success"));
    }

    /**
     * Ativa ou desativa um participante de grupo de jogo.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("@gameGroupParticipantService.canManageParticipant(#id, authentication.name)")
    @Operation(summary = "Ativar/Desativar participante", description = "Ativa ou desativa um participante de grupo de jogo")
    public ResponseEntity<ResponseApi<GameGroupParticipantResponse>> toggleStatus(
            @Parameter(description = "ID do participante") @PathVariable UUID id) {
        
        GameGroupParticipant participant = gameGroupParticipantService.toggleActiveStatus(id);
        GameGroupParticipantResponse participantResponse = gameGroupParticipantMapper.toResponse(participant);
        
        String message = participantResponse.getIsActive() ? 
                messageUtil.getMessage("controller.gamegroupparticipant.activated.success") : 
                messageUtil.getMessage("controller.gamegroupparticipant.deactivated.success");
        
        return ResponseUtil.okWithSuccess(participantResponse, message);
    }

    /**
     * Remove um participante de um grupo de jogo.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@gameGroupParticipantService.canManageParticipant(#id, authentication.name)")
    @Operation(summary = "Remover participante", description = "Remove um participante de um grupo de jogo")
    public ResponseEntity<ResponseApi<Void>> delete(
            @Parameter(description = "ID do participante") @PathVariable UUID id) {
        
        gameGroupParticipantService.removeParticipant(id);
        
        return ResponseUtil.okWithSuccess(
                null,
                messageUtil.getMessage("controller.gamegroupparticipant.deleted.success")
        );
    }



    /**
     * Constrói os parâmetros de query para paginação.
     */
    private String buildQueryParams(UUID gameGroupId, UUID userId, String role, Boolean isActive) {
        StringBuilder params = new StringBuilder();
        
        if (gameGroupId != null) {
            params.append("gameGroupId=").append(gameGroupId).append("&");
        }
        if (userId != null) {
            params.append("userId=").append(userId).append("&");
        }
        if (role != null && !role.trim().isEmpty()) {
            params.append("role=").append(role).append("&");
        }
        if (isActive != null) {
            params.append("isActive=").append(isActive).append("&");
        }
        
        // Remove o último '&' se existir
        if (params.length() > 0) {
            params.setLength(params.length() - 1);
        }
        
        return params.toString();
    }
}