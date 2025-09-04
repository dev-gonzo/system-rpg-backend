package br.com.systemrpg.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de introspecção de token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenIntrospectRequest {

    @NotBlank(message = "{validation.token.required}")
    private String token;
}
