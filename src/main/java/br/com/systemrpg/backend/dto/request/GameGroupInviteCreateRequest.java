package br.com.systemrpg.backend.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de convites de grupos de jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameGroupInviteCreateRequest {

    @NotBlank(message = "{validation.gameGroupInvite.role.required}")
    private String role; // PLAYER ou GUEST

    @NotNull(message = "{validation.gameGroupInvite.isUniqueUse.required}")
    private Boolean isUniqueUse; // Se o convite é de uso único

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt; // Data de expiração (opcional)

    // Getters manuais para resolver problemas do Lombok
    public String getRole() {
        return role;
    }

    public Boolean getIsUniqueUse() {
        return isUniqueUse;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}