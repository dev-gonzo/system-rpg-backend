package br.com.systemrpg.backend.controller;

import br.com.systemrpg.backend.domain.entity.Adventure;
import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.constants.MessageConstants;
import br.com.systemrpg.backend.dto.request.AdventureCreateRequest;
import br.com.systemrpg.backend.dto.response.AdventureResponse;
import br.com.systemrpg.backend.dto.response.ResponseApi;
import br.com.systemrpg.backend.mapper.AdventureMapper;
import br.com.systemrpg.backend.service.AdventureService;
import br.com.systemrpg.backend.service.GameGroupService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/adventures")
@RequiredArgsConstructor
@Validated
@Tag(name = "Adventures", description = "Operações relacionadas às aventuras dentro de grupos de jogo")
@SecurityRequirement(name = "bearerAuth")
public class AdventureController {

    private final AdventureService adventureService;
    private final GameGroupService gameGroupService;
    private final AdventureMapper adventureMapper;
    private final MessageUtil messageUtil;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Criar aventura", description = "Cria uma nova aventura dentro de um grupo de jogo (apenas MASTER)")
    @ApiResponse(responseCode = "201", description = "Aventura criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas MASTER do grupo pode criar aventuras")
    @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    public ResponseEntity<ResponseApi<AdventureResponse>> create(
            @Parameter(description = "Dados da aventura")
            @Valid @RequestBody AdventureCreateRequest request,
            HttpServletRequest httpRequest) {

        // Autorização manual para evitar falha de SpEL ao acessar o corpo da requisição
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!gameGroupService.isGroupOwner(request.getGameGroupId(), username)) {
            throw new AccessDeniedException(messageUtil.getMessage(MessageConstants.FORBIDDEN));
        }

        UUID createdByUserId = (UUID) httpRequest.getAttribute("userId");
        if (createdByUserId == null) {
            throw new IllegalStateException("ID do usuário não encontrado na requisição");
        }

        GameGroup group = gameGroupService.findById(request.getGameGroupId());
        Adventure adventureEntity = adventureMapper.toEntity(request, group, createdByUserId);
        Adventure createdAdventure = adventureService.createAdventure(adventureEntity, createdByUserId);

        AdventureResponse response = adventureMapper.toResponse(createdAdventure);
        String message = messageUtil.getMessage("controller.adventure.created.success");
        return ResponseUtil.createdWithSuccess(response, message);
    }

    @GetMapping
    @PreAuthorize("@gameGroupService.canViewGroup(#gameGroupId, authentication.name)")
    @Operation(summary = "Listar aventuras do grupo (paginado)", description = "Lista aventuras por grupo usando query param e paginação")
    @ApiResponse(responseCode = "200", description = "Lista de aventuras retornada com sucesso")
    public ResponseEntity<ResponseApi<java.util.List<AdventureResponse>>> listByGameGroupWithQuery(
            @Parameter(description = "ID do grupo de jogo") @RequestParam java.util.UUID gameGroupId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Adventure> page = adventureService.listByGameGroupId(gameGroupId, pageable);
        java.util.List<AdventureResponse> responses = page.getContent().stream()
                .map(adventureMapper::toResponse)
                .toList();
        String message = messageUtil.getMessage("controller.adventure.list.success");
        return ResponseUtil.okWithSuccess(responses, message);
    }

    @GetMapping("/")
    @PreAuthorize("@gameGroupService.canViewGroup(#gameGroupId, authentication.name)")
    @Operation(summary = "Listar aventuras do grupo (barra)", description = "Alias com barra final para evitar 405 em clientes que acrescentam /")
    @ApiResponse(responseCode = "200", description = "Lista de aventuras retornada com sucesso")
    public ResponseEntity<ResponseApi<java.util.List<AdventureResponse>>> listByGameGroupWithQueryTrailingSlash(
            @Parameter(description = "ID do grupo de jogo") @RequestParam java.util.UUID gameGroupId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Adventure> page = adventureService.listByGameGroupId(gameGroupId, pageable);
        java.util.List<AdventureResponse> responses = page.getContent().stream()
                .map(adventureMapper::toResponse)
                .toList();
        String message = messageUtil.getMessage("controller.adventure.list.success");
        return ResponseUtil.okWithSuccess(responses, message);
    }
    @GetMapping("/group/{gameGroupId}")
    @PreAuthorize("@gameGroupService.canViewGroup(#p0, authentication.name)")
    @Operation(summary = "Listar aventuras por grupo", description = "Lista aventuras de um grupo de jogo (participante ou MASTER)")
    @ApiResponse(responseCode = "200", description = "Lista de aventuras retornada com sucesso")
    public ResponseEntity<ResponseApi<java.util.List<AdventureResponse>>> listByGameGroup(
            @Parameter(description = "ID do grupo de jogo") @PathVariable java.util.UUID gameGroupId) {

        java.util.List<br.com.systemrpg.backend.domain.entity.Adventure> adventures = adventureService.listByGameGroupId(gameGroupId);
        java.util.List<AdventureResponse> responses = adventures.stream()
                .map(adventureMapper::toResponse)
                .toList();
        String message = messageUtil.getMessage("controller.adventure.list.success");
        return ResponseUtil.okWithSuccess(responses, message);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@adventureService.canManageAdventure(#p0, authentication.name)")
    @Operation(summary = "Editar aventura", description = "Atualiza título/descrição de uma aventura (apenas MASTER)")
    @ApiResponse(responseCode = "200", description = "Aventura atualizada com sucesso")
    public ResponseEntity<ResponseApi<AdventureResponse>> update(
            @Parameter(description = "ID da aventura") @PathVariable java.util.UUID id,
            @Parameter(description = "Dados para atualização") @jakarta.validation.Valid @RequestBody br.com.systemrpg.backend.dto.request.AdventureUpdateRequest request) {

        br.com.systemrpg.backend.domain.entity.Adventure updated = adventureService.updateAdventure(id, request);
        AdventureResponse response = adventureMapper.toResponse(updated);
        String message = messageUtil.getMessage("controller.adventure.updated.success");
        return ResponseUtil.okWithSuccess(response, message);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@adventureService.canManageAdventure(#p0, authentication.name)")
    @Operation(summary = "Apagar aventura", description = "Apaga (soft delete) uma aventura de um grupo (apenas MASTER)")
    @ApiResponse(responseCode = "200", description = "Aventura apagada com sucesso")
    public ResponseEntity<ResponseApi<Void>> delete(
            @Parameter(description = "ID da aventura") @PathVariable java.util.UUID id) {

        adventureService.deleteAdventure(id);
        String message = messageUtil.getMessage("controller.adventure.deleted.success");
        return ResponseUtil.okWithSuccess(null, message);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@adventureService.canViewAdventure(#p0, authentication.name)")
    @Operation(summary = "Buscar aventura por ID", description = "Retorna detalhes de uma aventura se você participa do grupo")
    @ApiResponse(responseCode = "200", description = "Aventura encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Aventura não encontrada")
    public ResponseEntity<ResponseApi<AdventureResponse>> findById(
            @Parameter(description = "ID da aventura") @PathVariable java.util.UUID id) {

        Adventure adventure = adventureService.findById(id);
        AdventureResponse response = adventureMapper.toResponse(adventure);
        String message = messageUtil.getMessage("controller.adventure.list.success");
        return ResponseUtil.okWithSuccess(response, message);
    }
}