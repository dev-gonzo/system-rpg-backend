package br.com.systemrpg.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar uma chave individual no JWKS (JSON Web Key Set).
 * Representa uma chave pública RSA no formato JWK padrão.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwkKey {

    /**
     * Tipo da chave (Key Type).
     * Para RSA, sempre "RSA".
     */
    @JsonProperty("kty")
    private String keyType;
    
    /**
     * Uso da chave (Key Use).
     * "sig" para assinatura, "enc" para criptografia.
     */
    @JsonProperty("use")
    private String keyUse;
    
    /**
     * Identificador único da chave (Key ID).
     * Usado para identificar qual chave foi usada para assinar um token.
     */
    @JsonProperty("kid")
    private String keyId;
    
    /**
     * Algoritmo usado com esta chave.
     * Ex: "RS256", "RS384", "RS512".
     */
    @JsonProperty("alg")
    private String algorithm;
    
    /**
     * Módulo da chave pública RSA (componente n).
     * Codificado em Base64URL.
     */
    @JsonProperty("n")
    private String modulus;
    
    /**
     * Expoente da chave pública RSA (componente e).
     * Geralmente "AQAB" (65537 em Base64URL).
     */
    @JsonProperty("e")
    private String exponent;
    
    /**
     * Cria uma JWK para chave RSA de assinatura.
     */
    public static JwkKey createRsaSigningKey(String keyId, String algorithm, String modulus, String exponent) {
        return JwkKey.builder()
                .keyType("RSA")
                .keyUse("sig")
                .keyId(keyId)
                .algorithm(algorithm)
                .modulus(modulus)
                .exponent(exponent)
                .build();
    }
}
