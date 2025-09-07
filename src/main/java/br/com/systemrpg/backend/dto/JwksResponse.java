package br.com.systemrpg.backend.dto;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta do endpoint JWKS (JSON Web Key Set).
 * Representa um conjunto de chaves públicas no formato padrão JWKS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwksResponse {

    /**
     * Array de chaves públicas no formato JWK.
     * Cada chave representa uma chave pública que pode ser usada
     * para verificar a assinatura de tokens JWT.
     */
    @JsonProperty("keys")
    private List<JwkKey> keys;
    
    // Setters manuais para resolver problemas do Lombok
    public void setKeys(List<JwkKey> keys) {
        this.keys = keys;
    }
    
    /**
     * Cria uma resposta JWKS com uma lista de chaves.
     */
    public static JwksResponse of(List<JwkKey> keys) {
        JwksResponse response = new JwksResponse();
        response.setKeys(keys);
        return response;
    }
    
    /**
     * Cria uma resposta JWKS com uma única chave.
     */
    public static JwksResponse of(JwkKey key) {
        JwksResponse response = new JwksResponse();
        response.setKeys(Arrays.asList(key));
        return response;
    }
}
