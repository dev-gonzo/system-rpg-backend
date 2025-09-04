package br.com.systemrpg.backend.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de introspecção de token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenIntrospectResponse {

    private Boolean active;
    
    private Map<String, Object> claims;
    
    private String error;
    
    /**
     * Cria uma resposta para token ativo.
     */
    public static TokenIntrospectResponse active(Map<String, Object> claims) {
        return TokenIntrospectResponse.builder()
                .active(true)
                .claims(claims)
                .build();
    }
    
    /**
     * Cria uma resposta para token inativo.
     */
    public static TokenIntrospectResponse inactive(String error) {
        return TokenIntrospectResponse.builder()
                .active(false)
                .error(error)
                .build();
    }
}
