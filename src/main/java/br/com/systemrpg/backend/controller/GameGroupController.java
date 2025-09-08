package br.com.systemrpg.backend.controller;

import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.domain.entity.GameGroupInvite;
import br.com.systemrpg.backend.dto.hateoas.GameGroupHateoasResponse;
import br.com.systemrpg.backend.dto.request.GameGroupCreateRequest;
import br.com.systemrpg.backend.dto.request.GameGroupInviteCreateRequest;
import br.com.systemrpg.backend.dto.request.GameGroupUpdateRequest;
import br.com.systemrpg.backend.dto.response.GameGroupInviteResponse;
import br.com.systemrpg.backend.dto.response.GameGroupResponse;
import br.com.systemrpg.backend.dto.response.ResponseApi;
import br.com.systemrpg.backend.hateoas.HateoasLinkBuilder;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import br.com.systemrpg.backend.mapper.GameGroupHateoasMapper;
import br.com.systemrpg.backend.mapper.GameGroupInviteMapper;
import br.com.systemrpg.backend.mapper.GameGroupMapper;
import br.com.systemrpg.backend.mapper.GameGroupMemberMapper;
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
            @Parameter(description = "ID do grupo de jogo") @PathVariable UUID id) {
        
        GameGroup gameGroup = gameGroupService.findById(id);
        GameGroupResponse gameGroupResponse = gameGroupMapper.toResponse(gameGroup, gameGroupMemberMapper);
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