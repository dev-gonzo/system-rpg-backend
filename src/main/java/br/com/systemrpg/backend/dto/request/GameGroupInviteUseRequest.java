package br.com.systemrpg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para usar um convite de grupo de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupInviteUseRequest {

    @NotBlank(message = "{validation.gameGroupInvite.inviteCode.required}")
    @Size(min = 8, max = 32, message = "{validation.gameGroupInvite.inviteCode.size}")
    private String inviteCode;
}