package br.com.systemrpg.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.TokenValidationUtil;
import br.com.systemrpg.backend.util.ValidationUtil;
import br.com.systemrpg.backend.exception.AuthenticationServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.systemrpg.backend.domain.entity.TokenBlacklist;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.AuthResponse;
import br.com.systemrpg.backend.dto.LoginRequest;
import br.com.systemrpg.backend.dto.RefreshTokenRequest;
import br.com.systemrpg.backend.dto.JwkKey;
import br.com.systemrpg.backend.dto.JwksResponse;
import br.com.systemrpg.backend.dto.TokenIntrospectRequest;
import br.com.systemrpg.backend.dto.TokenIntrospectResponse;
import br.com.systemrpg.backend.repository.TokenBlacklistRepository;
import br.com.systemrpg.backend.repository.UserRepository;
import br.com.systemrpg.backend.util.TokenUtils;
import br.com.systemrpg.backend.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de autenticação responsável por login, refresh token e logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final MessageUtil messageUtil;
    private final TokenValidationUtil tokenValidationUtil;

    /**
     * Realiza o login do usuário.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticateUser(request);
            User user = validateAndGetUser(request.getUsername());
            
            updateUserLastLogin(user);
            
            return createLoginResponse(authentication, user);

        } catch (AuthenticationException e) {
            log.error("Falha na autenticação para o usuário: {} - Erro: {} - Tipo: {}", 
                    request.getUsername(), e.getMessage(), e.getClass().getSimpleName());
            throw new BadCredentialsException(messageUtil.getMessage("service.auth.credentials.invalid"));
        }
    }

    /**
     * Cria a resposta de login com tokens.
     */
    private AuthResponse createLoginResponse(Authentication authentication, User user) {
        List<String> roles = extractRolesFromAuthentication(authentication);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return buildAuthResponse(accessToken, refreshToken, user, roles);
    }

    /**
     * Autentica o usuário usando o AuthenticationManager.
     */
    private Authentication authenticateUser(LoginRequest request) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
    }

    /**
     * Valida e obtém o usuário pelo username.
     */
    private User validateAndGetUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException(messageUtil
                    .getMessage("service.auth.user.not.found")));

        if (!user.getIsActive()) {
            log.warn("Tentativa de login com usuário inativo: {}", user.getUsername());
            throw new BadCredentialsException(messageUtil
                    .getMessage("service.auth.user.inactive"));
        }

        return user;
    }

    /**
     * Extrai as roles da autenticação.
     */
    private List<String> extractRolesFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
    }

    /**
     * Atualiza o último login do usuário.
     */
    private void updateUserLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Renova o access token usando o refresh token.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String accessToken = request.getAccessToken();

        validateRefreshToken(refreshToken);
        processAccessTokenBlacklist(accessToken);
        
        User user = getUserFromRefreshToken(refreshToken);
        validateUserForRefresh(refreshToken, user);
        
        return generateNewTokens(user);
    }

    /**
     * Gera novos tokens para o usuário.
     */
    private AuthResponse generateNewTokens(User user) {
        List<String> roles = getUserRoles(user);
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return buildAuthResponse(newAccessToken, newRefreshToken, user, roles);
    }

    /**
     * Valida o refresh token.
     */
    private void validateRefreshToken(String refreshToken) {
        if (!tokenValidationUtil.isTokenValid(refreshToken, jwtService.extractUsername(refreshToken))) {
            throw new BadCredentialsException(tokenValidationUtil.getTokenErrorMessage(refreshToken));
        }
        
        if (!tokenValidationUtil.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException(messageUtil
                    .getMessage("service.auth.refresh.token.invalid"));
        }
    }

    /**
     * Processa o access token para blacklist se fornecido.
     */
    private void processAccessTokenBlacklist(String accessToken) {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            try {
                String accessTokenUserId = jwtService.extractUserId(accessToken);
                UUID accessTokenUserIdUuid = UUID.fromString(accessTokenUserId);
                blacklistToken(accessToken, "Access token invalidado durante refresh", accessTokenUserIdUuid);
            } catch (Exception e) {
                log.warn("Erro ao processar accessToken durante refresh: {}", e.getMessage());
                // Continua o processo mesmo se houver erro com o accessToken
            }
        }
    }

    /**
     * Obtém o usuário a partir do refresh token.
     */
    private User getUserFromRefreshToken(String refreshToken) {
        String userIdStr = jwtService.extractUserId(refreshToken);
        UUID userId = UUID.fromString(userIdStr);

        return userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException(messageUtil
                    .getMessage("service.auth.user.not.found")));
    }

    /**
     * Valida o usuário para refresh token.
     */
    private void validateUserForRefresh(String refreshToken, User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPasswordHash())
            .authorities(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList()))
            .build();

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadCredentialsException(messageUtil
                    .getMessage("service.auth.refresh.token.invalid"));
        }

        if (!user.getIsActive()) {
            throw new BadCredentialsException(messageUtil
                    .getMessage("service.auth.user.inactive"));
        }
    }

    /**
     * Realiza o logout do usuário.
     */
    @Transactional
    public void logout(String authHeader) {
        try {
            String accessToken = TokenUtils.extractToken(authHeader);
            String username = jwtService.extractUsername(accessToken);
            
            // Validar access token
            if (!tokenValidationUtil.isTokenValid(accessToken, username)) {
                log.warn("Tentativa de logout com token inválido: {}", tokenValidationUtil.maskToken(accessToken));
                throw new BadCredentialsException(tokenValidationUtil.getTokenErrorMessage(accessToken));
            }

            String userIdStr = jwtService.extractUserId(accessToken);
            UUID userId = UUID.fromString(userIdStr);

            blacklistToken(accessToken, "Logout", userId);
            
            log.info("Logout realizado com sucesso para o usuário: {}", username);

        } catch (Exception e) {
            log.error("Erro durante o logout", e);
            throw new AuthenticationServiceException(messageUtil
                    .getMessage("service.auth.logout.error"), e);
        }
    }

    /**
     * Adiciona um token à blacklist.
     */
    private void blacklistToken(String token, String reason, UUID userId) {
        String tokenHash = jwtService.generateTokenHash(token);
        LocalDateTime expiresAt = jwtService.convertToLocalDateTime(jwtService.extractExpiration(token));

        TokenBlacklist blacklistEntry = TokenBlacklist.builder()
            .tokenHash(tokenHash)
            .userId(userId)
            .reason(reason)
            .expiresAt(expiresAt)
            .createdAt(LocalDateTime.now())
            .build();

        tokenBlacklistRepository.save(blacklistEntry);
    }

    /**
     * Cria Response de autenticação.
     */
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user, List<String> roles) {
        LocalDateTime expiresAt = jwtService.convertToLocalDateTime(jwtService.extractExpiration(accessToken));
        long expiresIn = SecurityConstants.ACCESS_TOKEN_EXPIRES_IN_SECONDS;
        AuthResponse.UserInfo userInfo = buildUserInfo(user, roles);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .expiresAt(expiresAt)
            .user(userInfo)
            .build();
    }

    /**
     * Constrói as informações do usuário para a resposta.
     */
    private AuthResponse.UserInfo buildUserInfo(User user, List<String> roles) {
        return AuthResponse.UserInfo.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .roles(roles)
            .isActive(user.getIsActive())
            .isEmailVerified(user.getIsEmailVerified())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    /**
     * Obtém as roles do usuário.
     */
    private List<String> getUserRoles(User user) {
        return user.getRoles().stream()
            .map(role -> "ROLE_" + role.getName())
            .collect(Collectors.toList());
    }

    /**
     * Realiza a introspecção de um token JWT.
     * 
     * @param request Request contendo o token a ser validado
     * @return TokenIntrospectResponse com o resultado da validação
     */
    public TokenIntrospectResponse introspect(TokenIntrospectRequest request) {
        try {
            String token = request.getToken();
            
            if (!ValidationUtil.isValidString(token)) {
                return TokenIntrospectResponse.inactive(
                    messageUtil.getMessage("service.auth.token.empty")
                );
            }

            // Remove o prefixo "Bearer " se presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Usa o JwtService para validar e extrair claims
            Map<String, Object> claims = jwtService.introspectToken(token);
            
            if (claims == null || claims.isEmpty()) {
                // Token inválido - pode ser expirado, blacklisted ou com assinatura inválida
                String errorMessage = determineTokenErrorMessage(token);
                return TokenIntrospectResponse.inactive(errorMessage);
            }

            // Token válido - retorna as claims
            return TokenIntrospectResponse.active(claims);
            
        } catch (Exception e) {
            log.error("Erro durante introspecção do token: {}", e.getMessage(), e);
            return TokenIntrospectResponse.inactive(
                messageUtil.getMessage("service.auth.introspect.error")
            );
        }
    }
    
    /**
     * Gera o JWKS (JSON Web Key Set) com as chaves públicas.
     * 
     * @return JwksResponse contendo as chaves públicas para validação de tokens
     */
    public JwksResponse generateJwks() {
        try {
            JwkKey jwkKey = jwtService.generateJwkKey();
            
            if (jwkKey == null) {
                log.warn("Não foi possível gerar JWK - algoritmo não suportado ou configuração inválida");
                return JwksResponse.of(List.of());
            }
            
            log.info("JWKS gerado com sucesso com key ID: {}", jwkKey.getKeyId());
            return JwksResponse.of(jwkKey);
            
        } catch (Exception e) {
            log.error("Erro ao gerar JWKS: {}", e.getMessage());
            return JwksResponse.of(List.of());
        }
    }
    
    /**
     * Determina a mensagem de erro apropriada para um token inválido.
     */
    private String determineTokenErrorMessage(String token) {
        return tokenValidationUtil.getTokenErrorMessage(token);
    }
}
