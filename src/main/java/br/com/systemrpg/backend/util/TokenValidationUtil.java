package br.com.systemrpg.backend.util;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import br.com.systemrpg.backend.service.JwtService;
import br.com.systemrpg.backend.constants.ValidationConstants;
import static br.com.systemrpg.backend.util.ValidationUtil.isValidString;

/**
 * Utilitário para centralizar validações de token.
 * Reduz a duplicação de código ao validar tokens JWT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidationUtil {

    private final JwtService jwtService;
    private final MessageUtil messageUtil;

    /**
     * Valida se um token é válido para uso geral.
     *
     * @param token o token a ser validado
     * @param username o nome do usuário
     * @return true se o token é válido, false caso contrário
     */
    public boolean isTokenValid(String token, String username) {
        if (!isValidString(token)) {
            log.warn("Token vazio ou nulo recebido");
            return false;
        }

        if (!isValidString(username)) {
            log.warn("Username vazio ou nulo para validação de token");
            return false;
        }

        try {
            // Verifica se o token não está na blacklist
            if (jwtService.isTokenBlacklisted(token)) {
                log.warn("Token na blacklist: {}", maskToken(token));
                return false;
            }

            // Verifica se o token não está expirado
            if (jwtService.isTokenExpired(token)) {
                log.warn("Token expirado: {}", maskToken(token));
                return false;
            }

            // Verifica se o username do token corresponde
            String tokenUsername = jwtService.extractUsername(token);
            if (!username.equals(tokenUsername)) {
                log.warn("Username do token não corresponde. Esperado: {}, Token: {}", username, tokenUsername);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("Erro ao validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida se um token é do tipo ACCESS.
     *
     * @param token o token a ser validado
     * @return true se é um access token válido, false caso contrário
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = jwtService.extractTokenType(token);
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            log.warn("Erro ao extrair tipo do token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida se um token é do tipo REFRESH.
     *
     * @param token o token a ser validado
     * @return true se é um refresh token válido, false caso contrário
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = jwtService.extractTokenType(token);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            log.warn("Erro ao extrair tipo do token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém uma mensagem de erro apropriada para um token inválido.
     *
     * @param token o token inválido
     * @return mensagem de erro localizada
     */
    public String getTokenErrorMessage(String token) {
        try {
            if (jwtService.isTokenBlacklisted(token)) {
                return messageUtil.getMessage("service.auth.token.blacklisted");
            } else if (jwtService.isTokenExpired(token)) {
                return messageUtil.getMessage("service.auth.token.expired");
            } else {
                return messageUtil.getMessage("service.auth.token.invalid.signature");
            }
        } catch (Exception e) {
            return messageUtil.getMessage("service.auth.token.invalid.signature");
        }
    }

    /**
     * Mascara um token para logs de segurança.
     *
     * @param token o token a ser mascarado
     * @return token mascarado
     */
    public String maskToken(String token) {
        if (token == null || token.length() < ValidationConstants.TOKEN_MIN_LENGTH_FOR_MASK) {
            return "***";
        }
        return token.substring(0, ValidationConstants.TOKEN_MASK_PREFIX_LENGTH) + "..." + 
               token.substring(token.length() - ValidationConstants.TOKEN_MASK_SUFFIX_LENGTH);
    }

    /**
     * Extrai o header Authorization de uma requisição.
     *
     * @param authHeader o header de autorização
     * @return o token extraído ou null se inválido
     */
    public String extractTokenFromAuthHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
