package br.com.systemrpg.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.dto.response.GameGroupParticipantResponse;
import br.com.systemrpg.backend.dto.response.SuccessResponse;
import br.com.systemrpg.backend.mapper.GameGroupParticipantMapper;
import br.com.systemrpg.backend.service.GameGroupInviteService;
import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável por gerenciar o uso de convites de grupos de jogo.
 */
@RestController
@RequestMapping("/api/v1/game-group-invites")
@RequiredArgsConstructor
@Validated
@Tag(name = "Game Group Invites", description = "Operações relacionadas ao uso de convites de grupos de jogo")
@SecurityRequirement(name = "bearerAuth")
public class GameGroupInviteController {

    private final GameGroupInviteService gameGroupInviteService;
    private final MessageUtil messageUtil;
    private final GameGroupParticipantMapper gameGroupParticipantMapper;

    /**
     * Usa um convite para entrar em um grupo de jogo.
     */
    @PostMapping("/{inviteCode}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Usar convite para entrar no grupo", description = "Usa um código de convite para entrar em um grupo de jogo")
    @ApiResponse(responseCode = "200", description = "Convite usado com sucesso, usuário adicionado ao grupo")
    @ApiResponse(responseCode = "400", description = "Convite inválido, expirado, já usado ou limite de participantes atingido")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Convite não encontrado")
    @ApiResponse(responseCode = "409", description = "Usuário já participa do grupo")
    public ResponseEntity<SuccessResponse<GameGroupParticipantResponse>> useInvite(
            @Parameter(description = "Código do convite") @PathVariable String inviteCode,
            HttpServletRequest httpRequest) {
        
        try {
            String username = httpRequest.getRemoteUser();
            GameGroupParticipant participant = gameGroupInviteService.useInvite(inviteCode, username);
            GameGroupParticipantResponse response = gameGroupParticipantMapper.toResponse(participant);
            
            return ResponseUtil.okWithSuccess(response, messageUtil.getMessage("controller.gamegroupinvite.used.success"));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse<>(e.getMessage(), null));
        }
    }
}