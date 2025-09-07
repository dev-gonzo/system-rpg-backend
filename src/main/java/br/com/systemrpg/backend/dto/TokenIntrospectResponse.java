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
    
    // Setters manuais para resolver problemas do Lombok
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    /**
     * Cria uma resposta para token ativo.
     */
    public static TokenIntrospectResponse active(Map<String, Object> claims) {
        TokenIntrospectResponse response = new TokenIntrospectResponse();
        response.setActive(true);
        response.setClaims(claims);
        return response;
    }
    
    /**
     * Cria uma resposta para token inativo.
     */
    public static TokenIntrospectResponse inactive(String error) {
        TokenIntrospectResponse response = new TokenIntrospectResponse();
        response.setActive(false);
        response.setError(error);
        return response;
    }
}
