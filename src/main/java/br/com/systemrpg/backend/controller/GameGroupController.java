package br.com.systemrpg.backend.controller;

import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.domain.entity.GameGroupInvite;
import br.com.systemrpg.backend.dto.hateoas.GameGroupHateoasResponse;
import br.com.systemrpg.backend.dto.request.AdventureCreateRequest;
import br.com.systemrpg.backend.dto.response.AdventureResponse;
import br.com.systemrpg.backend.mapper.AdventureMapper;
import br.com.systemrpg.backend.service.AdventureService;
import br.com.systemrpg.backend.dto.request.GameGroupCreateRequest;
import br.com.systemrpg.backend.dto.request.GameGroupInviteCreateRequest;
import br.com.systemrpg.backend.dto.request.GameGroupUpdateRequest;
import br.com.systemrpg.backend.dto.response.GameGroupInviteResponse;
import br.com.systemrpg.backend.dto.response.GameGroupResponse;
import br.com.systemrpg.backend.dto.response.ResponseApi;
import br.com.systemrpg.backend.dto.request.GameGroupParticipantCreateRequest;
import br.com.systemrpg.backend.dto.request.GameGroupParticipantAddRequest;
import br.com.systemrpg.backend.dto.response.GameGroupParticipantResponse;
import br.com.systemrpg.backend.hateoas.HateoasLinkBuilder;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import br.com.systemrpg.backend.mapper.GameGroupHateoasMapper;
import br.com.systemrpg.backend.mapper.GameGroupInviteMapper;
import br.com.systemrpg.backend.mapper.GameGroupMapper;
import br.com.systemrpg.backend.mapper.GameGroupMemberMapper;
import br.com.systemrpg.backend.mapper.GameGroupParticipantMapper;
import br.com.systemrpg.backend.service.GameGroupInviteService;
import br.com.systemrpg.backend.service.GameGroupService;
import br.com.systemrpg.backend.util.ResponseUtil;
import br.com.systemrpg.backend.util.MessageUtil;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller responsável por gerenciar grupos de jogo.
 */
@RestController
@RequestMapping("/api/v1/game-groups")
@RequiredArgsConstructor
@Validated
@Tag(name = "Game Groups", description = "Operações relacionadas aos grupos de jogo")
@SecurityRequirement(name = "bearerAuth")
public class GameGroupController {

    private final GameGroupService gameGroupService;
    private final GameGroupInviteService gameGroupInviteService;
    private final GameGroupMapper gameGroupMapper;
    private final GameGroupMemberMapper gameGroupMemberMapper;
    private final GameGroupHateoasMapper gameGroupHateoasMapper;
    private final GameGroupInviteMapper gameGroupInviteMapper;
    private final HateoasLinkBuilder hateoasLinkBuilder;
    private final MessageUtil messageUtil;
    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final br.com.systemrpg.backend.service.GameGroupParticipantService gameGroupParticipantService;
    private final GameGroupParticipantMapper gameGroupParticipantMapper;

    /**
     * Lista todos os grupos de jogo com paginação e filtros.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Listar grupos de jogo", description = "Lista todos os grupos de jogo com paginação e filtros opcionais")
    @ApiResponse(responseCode = "200", description = "Lista de grupos de jogo retornada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<ResponseApi<PagedHateoasResponse<GameGroupHateoasResponse>>> listAll(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Nome da campanha para filtrar") @RequestParam(required = false) String campaignName,
            @Parameter(description = "Sistema de jogo para filtrar") @RequestParam(required = false) String gameSystem,
            @Parameter(description = "Mundo/cenário para filtrar") @RequestParam(required = false) String settingWorld) {
        
        Page<GameGroup> gameGroups = gameGroupService.findByFilters(campaignName, gameSystem, settingWorld, pageable);
        String queryParams = buildQueryParams(campaignName, gameSystem, settingWorld);
        PagedHateoasResponse<GameGroupHateoasResponse> response = buildGameGroupListResponse(gameGroups, pageable, "/game-groups", queryParams);
        
        String message = messageUtil.getMessage("controller.gamegroup.list.success");
        
        return ResponseUtil.okWithSuccess(response, message);
    }

    /**
     * Lista apenas os grupos de jogo que o usuário participa (como owner, player ou guest).
     */
    @GetMapping("/my-groups")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Listar meus grupos de jogo", description = "Lista apenas os grupos de jogo que o usuário autenticado participa")
    @ApiResponse(responseCode = "200", description = "Lista de grupos do usuário retornada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<ResponseApi<PagedHateoasResponse<GameGroupHateoasResponse>>> listMyGroups(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpRequest) {
        
        String username = httpRequest.getUserPrincipal().getName();
        Page<GameGroup> gameGroups = gameGroupService.findMyGameGroups(username, pageable);
        PagedHateoasResponse<GameGroupHateoasResponse> response = buildGameGroupListResponse(gameGroups, pageable, "/game-groups/my-groups", "");
        
        String message = messageUtil.getMessage("controller.gamegroup.mygroups.success");
        
        return ResponseUtil.okWithSuccess(response, message);
    }

    /**
     * Cria uma nova aventura dentro de um grupo de jogo.
     */
    @PostMapping("/{id}/adventures")
    @PreAuthorize("@gameGroupService.isGroupOwner(#p0, authentication.name)")
    @Operation(summary = "Criar aventura no grupo", description = "Cria uma nova aventura dentro de um grupo de jogo (apenas MASTER)")
    @ApiResponse(responseCode = "201", description = "Aventura criada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas MASTER do grupo pode criar aventuras")
    @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    public ResponseEntity<ResponseApi<AdventureResponse>> createAdventureInGroup(
            @Parameter(description = "ID do grupo de jogo") @PathVariable java.util.UUID id,
            @Parameter(description = "Dados da aventura - sem necessidade de gameGroupId no corpo") @RequestBody AdventureCreateRequest request,
            HttpServletRequest httpRequest) {

        java.util.UUID createdByUserId = (java.util.UUID) httpRequest.getAttribute("userId");
        if (createdByUserId == null) {
            throw new IllegalStateException("ID do usuário não encontrado na requisição");
        }

        // Força o gameGroupId a partir do path, permitindo corpo sem este campo
        request.setGameGroupId(id);

        br.com.systemrpg.backend.domain.entity.GameGroup group = gameGroupService.findById(id);
        br.com.systemrpg.backend.domain.entity.Adventure adventureEntity = adventureMapper.toEntity(request, group, createdByUserId);
        br.com.systemrpg.backend.domain.entity.Adventure createdAdventure = adventureService.createAdventure(adventureEntity, createdByUserId);

        AdventureResponse response = adventureMapper.toResponse(createdAdventure);
        String message = messageUtil.getMessage("controller.adventure.created.success");
        return ResponseUtil.createdWithSuccess(response, message);
    }

    /**
     * Adiciona um participante ao grupo de jogo (alias aninhado).
     */
    @PostMapping("/{id}/participants")
    @PreAuthorize("@gameGroupService.isGroupOwner(#id, authentication.name)")
    @Operation(summary = "Adicionar participante ao grupo", description = "Adiciona um participante ao grupo de jogo (apenas MASTER/owner)")
    @ApiResponse(responseCode = "201", description = "Participante adicionado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas owner pode adicionar participantes")
    @ApiResponse(responseCode = "404", description = "Grupo ou usuário não encontrado")
    public ResponseEntity<ResponseApi<GameGroupParticipantResponse>> addParticipantToGroup(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id,
            @Parameter(description = "Dados do participante - informe userId e opcionalmente role") @Valid @RequestBody GameGroupParticipantAddRequest request) {

        GameGroupParticipantCreateRequest effectiveRequest = GameGroupParticipantCreateRequest.builder()
                .gameGroupId(id)
                .userId(request.getUserId())
                .role(request.getRole())
                .build();

        GameGroupParticipantResponse participant = gameGroupParticipantService.create(effectiveRequest);
        String message = messageUtil.getMessage("controller.gamegroupparticipant.created.success");
        return ResponseUtil.createdWithSuccess(participant, message);
    }

    /**
     * Ativa ou desativa um participante (alias aninhado).
     * Observação: o corpo enviado (como {"isActive": false}) é ignorado; a operação é um toggle.
     */
    @PatchMapping("/{groupId}/participants/{participantId}/status")
    @PreAuthorize("@gameGroupService.isGroupOwner(#groupId, authentication.name)")
    @Operation(summary = "Ativar/Desativar participante do grupo", description = "Alterna o status ativo de um participante (apenas MASTER/owner)")
    @ApiResponse(responseCode = "200", description = "Status do participante alterado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas owner pode gerenciar participantes")
    @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    public ResponseEntity<ResponseApi<br.com.systemrpg.backend.dto.response.GameGroupParticipantResponse>> toggleParticipantStatusInGroup(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID groupId,
            @Parameter(description = "ID do participante") @PathVariable UUID participantId) {

        br.com.systemrpg.backend.domain.entity.GameGroupParticipant participant = gameGroupParticipantService.toggleActiveStatus(participantId);
        br.com.systemrpg.backend.dto.response.GameGroupParticipantResponse participantResponse = gameGroupParticipantMapper.toResponse(participant);

        String message = participantResponse.getIsActive() ?
                messageUtil.getMessage("controller.gamegroupparticipant.activated.success") :
                messageUtil.getMessage("controller.gamegroupparticipant.deactivated.success");

        return ResponseUtil.okWithSuccess(participantResponse, message);
    }

    /**
     * Remove um participante do grupo (rota aninhada).
     * Exige que o usuário autenticado seja MASTER do grupo do participante.
     */
    @DeleteMapping("/{groupId}/participants/{participantId}")
    @PreAuthorize("@gameGroupService.isGroupOwner(#groupId, authentication.name)")
    @Operation(summary = "Remover participante do grupo", description = "Remove um participante do grupo de jogo (apenas MASTER/owner)")
    @ApiResponse(responseCode = "200", description = "Participante removido com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas owner pode remover participantes")
    @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    public ResponseEntity<ResponseApi<Void>> removeParticipantFromGroup(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID groupId,
            @Parameter(description = "ID do participante") @PathVariable UUID participantId) {

        // Suporta tanto participantId quanto userId na mesma rota.
        // 1) Tenta remover pelo participantId (se existir e pertencer ao grupo).
        try {
            br.com.systemrpg.backend.domain.entity.GameGroupParticipant participant = gameGroupParticipantService.findById(participantId);
            if (!participant.getGameGroup().getId().equals(groupId)) {
                throw new br.com.systemrpg.backend.exception.RecordNotFoundException(
                    messageUtil.getMessage("service.gameGroupParticipant.not.found", new Object[]{participantId}, java.util.Locale.getDefault())
                );
            }
            gameGroupParticipantService.removeParticipant(participantId);
        } catch (br.com.systemrpg.backend.exception.RecordNotFoundException ex) {
            // 2) Se não encontrar por participantId, trata como userId vinculado ao grupo.
            gameGroupParticipantService.removeParticipantByGroupAndUser(groupId, participantId);
        }

        return ResponseUtil.okWithSuccess(null, messageUtil.getMessage("controller.gamegroupparticipant.deleted.success"));
    }

    /**
     * Remove um participante do grupo por userId (rota aninhada de conveniência).
     * Útil quando o client possui apenas o userId e quer remover o vínculo do grupo.
     */
    @DeleteMapping("/{groupId}/participants/by-user/{userId}")
    @PreAuthorize("@gameGroupService.isGroupOwner(#groupId, authentication.name)")
    @Operation(summary = "Remover participante do grupo por userId", description = "Remove o participante do grupo usando o ID do usuário (apenas MASTER/owner)")
    @ApiResponse(responseCode = "200", description = "Participante removido com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas owner pode remover participantes")
    @ApiResponse(responseCode = "404", description = "Participante não encontrado no grupo")
    public ResponseEntity<ResponseApi<Void>> removeParticipantFromGroupByUser(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID groupId,
            @Parameter(description = "ID do usuário") @PathVariable UUID userId) {

        gameGroupParticipantService.removeParticipantByGroupAndUser(groupId, userId);
        return ResponseUtil.okWithSuccess(null, messageUtil.getMessage("controller.gamegroupparticipant.deleted.success"));
    }

    /**
     * Remove um participante do grupo por username (rota de conveniência).
     * Útil quando o client possui apenas o username e quer remover o vínculo do grupo.
     */
    @DeleteMapping("/{groupId}/participants/by-username/{username}")
    @PreAuthorize("@gameGroupService.isGroupOwner(#groupId, authentication.name)")
    @Operation(summary = "Remover participante do grupo por username", description = "Remove o participante do grupo usando o username (apenas MASTER/owner)")
    @ApiResponse(responseCode = "200", description = "Participante removido com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas owner pode remover participantes")
    @ApiResponse(responseCode = "404", description = "Participante não encontrado no grupo")
    public ResponseEntity<ResponseApi<Void>> removeParticipantFromGroupByUsername(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID groupId,
            @Parameter(description = "Username do usuário") @PathVariable String username) {

        gameGroupParticipantService.removeParticipantByGroupAndUsername(groupId, username);
        return ResponseUtil.okWithSuccess(null, messageUtil.getMessage("controller.gamegroupparticipant.deleted.success"));
    }

    /**
     * Constrói a resposta paginada com links HATEOAS para listas de grupos de jogo.
     */
    private PagedHateoasResponse<GameGroupHateoasResponse> buildGameGroupListResponse(Page<GameGroup> gameGroups, Pageable pageable, String basePath, String queryParams) {
        Page<GameGroupResponse> gameGroupResponses = gameGroups.map(gameGroup -> gameGroupMapper.toResponse(gameGroup, gameGroupMemberMapper));
        PagedHateoasResponse<GameGroupHateoasResponse> hateoasResponse = gameGroupHateoasMapper.toPagedHateoasResponse(gameGroupResponses);
        
        addIndividualGameGroupLinks(hateoasResponse, gameGroups);
        addCollectionLinks(hateoasResponse, pageable, basePath, queryParams);
        
        return hateoasResponse;
    }

    /**
     * Adiciona links HATEOAS individuais para cada grupo de jogo.
     */
    private void addIndividualGameGroupLinks(PagedHateoasResponse<GameGroupHateoasResponse> hateoasResponse, Page<GameGroup> gameGroups) {
        for (int i = 0; i < hateoasResponse.getContent().size(); i++) {
            GameGroupHateoasResponse gameGroupHateoas = hateoasResponse.getContent().get(i);
            GameGroup originalGameGroup = gameGroups.getContent().get(i);
            hateoasLinkBuilder.addIndividualGameGroupLinks(gameGroupHateoas, originalGameGroup.getId());
        }
    }

    /**
     * Adiciona links HATEOAS da coleção e paginação.
     */
    private void addCollectionLinks(PagedHateoasResponse<GameGroupHateoasResponse> hateoasResponse, Pageable pageable, String basePath, String queryParams) {
        hateoasLinkBuilder.addGameGroupLinks(hateoasResponse);
        hateoasLinkBuilder.addPaginationLinks(hateoasResponse, pageable, basePath, queryParams);
    }

    /**
     * Busca um grupo de jogo por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@gameGroupService.canViewGroup(#id, authentication.name)")
    @Operation(summary = "Buscar grupo por ID", description = "Busca um grupo de jogo pelo seu ID")
    public ResponseEntity<ResponseApi<GameGroupHateoasResponse>> findById(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id,
            @Parameter(description = "Incluir participantes inativos no retorno") @RequestParam(required = false) Boolean includeInactive) {
        
        GameGroup gameGroup = gameGroupService.findById(id);
        GameGroupResponse gameGroupResponse = gameGroupMapper.toResponse(gameGroup, gameGroupMemberMapper);

        // Se solicitado, incluir participantes inativos (apenas exclui deletados)
        if (Boolean.TRUE.equals(includeInactive) && gameGroup.getParticipants() != null) {
            List<br.com.systemrpg.backend.dto.response.GameGroupMemberResponse> allMembers = gameGroup.getParticipants().stream()
                    .filter(p -> p.getDeletedAt() == null)
                    .map(gameGroupMemberMapper::toResponse)
                    .collect(Collectors.toList());
            gameGroupResponse.setParticipants(allMembers);
        }

        GameGroupHateoasResponse hateoasResponse = gameGroupHateoasMapper.toHateoasResponse(gameGroupResponse);
        
        // Adicionar links HATEOAS
        hateoasLinkBuilder.addGameGroupLinks(hateoasResponse, id);
        
        String message = messageUtil.getMessage("controller.gamegroup.found.success");
        
        return ResponseUtil.okWithSuccess(hateoasResponse, message);
    }

    /**
     * Cria um novo grupo de jogo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Criar grupo de jogo", description = "Cria um novo grupo de jogo")
    @ApiResponse(responseCode = "201", description = "Grupo criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<ResponseApi<GameGroupHateoasResponse>> create(
            @Parameter(description = "Dados do grupo de jogo")
            @Valid @RequestBody GameGroupCreateRequest request,
            HttpServletRequest httpRequest) {
        
        // Obter ID do usuário autenticado via JWT
        UUID createdByUserId = (UUID) httpRequest.getAttribute("userId");
        if (createdByUserId == null) {
            throw new IllegalStateException("ID do usuário não encontrado na requisição");
        }
        
        GameGroup gameGroupEntity = gameGroupMapper.toEntity(request);
        GameGroup createdGroup = gameGroupService.createGameGroup(gameGroupEntity, createdByUserId);
        GameGroupResponse gameGroupResponse = gameGroupMapper.toResponse(createdGroup, gameGroupMemberMapper);
        GameGroupHateoasResponse hateoasResponse = gameGroupHateoasMapper.toHateoasResponse(gameGroupResponse);
        
        // Adicionar links HATEOAS
        hateoasLinkBuilder.addGameGroupLinks(hateoasResponse, createdGroup.getId());
        
        String message = messageUtil.getMessage("controller.gamegroup.created.success");
        
        return ResponseUtil.createdWithSuccess(hateoasResponse, message);
    }

    /**
     * Atualiza um grupo de jogo existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@gameGroupService.isGroupOwner(#id, authentication.name)")
    @Operation(summary = "Atualizar grupo de jogo", description = "Atualiza um grupo de jogo existente")
    public ResponseEntity<ResponseApi<GameGroupHateoasResponse>> update(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id,
            @Valid @RequestBody GameGroupUpdateRequest request) {
        
        GameGroup existingGroup = gameGroupService.findById(id);
        gameGroupMapper.updateEntityFromRequest(request, existingGroup);
        GameGroup updatedGroup = gameGroupService.updateGameGroup(id, existingGroup);
        GameGroupResponse gameGroupResponse = gameGroupMapper.toResponse(updatedGroup, gameGroupMemberMapper);
        GameGroupHateoasResponse hateoasResponse = gameGroupHateoasMapper.toHateoasResponse(gameGroupResponse);
        
        // Adicionar links HATEOAS
        hateoasLinkBuilder.addGameGroupLinks(hateoasResponse, id);
        
        String message = messageUtil.getMessage("controller.gamegroup.updated.success");
        
        return ResponseUtil.okWithSuccess(hateoasResponse, message);
    }

    /**
     * Ativa ou desativa um grupo de jogo.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("@gameGroupService.isGroupOwner(#id, authentication.name)")
    @Operation(summary = "Ativar/Desativar grupo", description = "Ativa ou desativa um grupo de jogo")
    public ResponseEntity<ResponseApi<GameGroupHateoasResponse>> toggleStatus(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id) {
        
        GameGroup gameGroup = gameGroupService.toggleActiveStatus(id);
        GameGroupResponse gameGroupResponse = gameGroupMapper.toResponse(gameGroup, gameGroupMemberMapper);
        GameGroupHateoasResponse hateoasResponse = gameGroupHateoasMapper.toHateoasResponse(gameGroupResponse);
        
        // Adicionar links HATEOAS
        hateoasLinkBuilder.addGameGroupLinks(hateoasResponse, id);
        
        String message = gameGroupResponse.getIsActive() ? 
                messageUtil.getMessage("controller.gamegroup.activated.success") : 
                messageUtil.getMessage("controller.gamegroup.deactivated.success");
        
        return ResponseUtil.okWithSuccess(hateoasResponse, message);
    }

    /**
     * Exclui um grupo de jogo.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@gameGroupService.isGroupOwner(#id, authentication.name)")
    @Operation(summary = "Excluir grupo de jogo", description = "Exclui um grupo de jogo")
    public ResponseEntity<ResponseApi<Void>> delete(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id) {
        
        gameGroupService.deleteGameGroup(id);
        
        return ResponseUtil.okWithSuccess(null, messageUtil.getMessage("controller.gamegroup.deleted.success"));
    }

    /**
     * Cria um convite para o grupo de jogo.
     */
    @PostMapping("/{id}/invites")
    @PreAuthorize("@gameGroupService.isGroupOwner(#id, authentication.name)")
    @Operation(summary = "Criar convite para grupo", description = "Cria um convite para participar do grupo de jogo (apenas owner)")
    @ApiResponse(responseCode = "201", description = "Convite criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas owner pode criar convites")
    @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    public ResponseEntity<ResponseApi<GameGroupInviteResponse>> createInvite(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id,
            @Parameter(description = "Dados do convite")
            @Valid @RequestBody GameGroupInviteCreateRequest request,
            HttpServletRequest httpRequest) {
        
        String username = httpRequest.getUserPrincipal().getName();
        
        // Converte string role para enum
        GameGroupInvite.InviteRole role = GameGroupInvite.InviteRole.valueOf(request.getRole().toUpperCase());
        
        GameGroupInvite invite = gameGroupInviteService.createInvite(
            id, username, role, request.getIsUniqueUse(), request.getExpiresAt());
        
        GameGroupInviteResponse response = gameGroupInviteMapper.toResponse(invite);
        
        String message = messageUtil.getMessage("controller.gamegroup.invite.created.success");
        
        return ResponseUtil.createdWithSuccess(response, message);
    }

    /**
     * Lista convites de um grupo de jogo.
     */
    @GetMapping("/{id}/invites")
    @PreAuthorize("@gameGroupService.isGroupOwner(#id, authentication.name)")
    @Operation(summary = "Listar convites do grupo", description = "Lista todos os convites de um grupo de jogo (apenas owner)")
    @ApiResponse(responseCode = "200", description = "Lista de convites retornada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas owner pode ver convites")
    @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    public ResponseEntity<ResponseApi<List<GameGroupInviteResponse>>> listInvites(
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id) {
        
        List<GameGroupInvite> invites = gameGroupInviteService.findByGameGroupId(id);
        List<GameGroupInviteResponse> responses = invites.stream()
            .map(gameGroupInviteMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseUtil.okWithSuccess(responses, messageUtil.getMessage("controller.gamegroup.invites.list.success"));
    }

    /**
     * Remove um convite.
     */
    @DeleteMapping("/invites/{inviteId}")
    @PreAuthorize("@gameGroupInviteService.canDeleteInvite(#inviteId, authentication.name)")
    @Operation(summary = "Remover convite", description = "Remove um convite de grupo de jogo")
    @ApiResponse(responseCode = "200", description = "Convite removido com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Convite não encontrado")
    public ResponseEntity<ResponseApi<Void>> deleteInvite(
            @Parameter(description = "ID do convite") @PathVariable UUID inviteId,
            HttpServletRequest httpRequest) {
        
        String username = httpRequest.getUserPrincipal().getName();
        gameGroupInviteService.deleteInvite(inviteId, username);
        
        return ResponseUtil.okWithSuccess(null, messageUtil.getMessage("controller.gamegroup.invite.deleted.success"));
    }

    /**
     * Constrói os parâmetros de query para paginação.
     */
    private String buildQueryParams(String campaignName, String gameSystem, String settingWorld) {
        StringBuilder params = new StringBuilder();
        
        if (campaignName != null && !campaignName.trim().isEmpty()) {
            params.append("campaignName=").append(campaignName).append("&");
        }
        if (gameSystem != null && !gameSystem.trim().isEmpty()) {
            params.append("gameSystem=").append(gameSystem).append("&");
        }
        if (settingWorld != null && !settingWorld.trim().isEmpty()) {
            params.append("settingWorld=").append(settingWorld).append("&");
        }
        
        // Remove o último '&' se existir
        if (params.length() > 0) {
            params.setLength(params.length() - 1);
        }
        
        return params.toString();
    }
}