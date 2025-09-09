package br.com.systemrpg.backend.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.systemrpg.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.TokenValidationUtil;

/**
 * Filtro JWT para interceptar e validar tokens de autenticação.
 * Este filtro roda em todas as requisições. Se um token JWT válido for encontrado,
 * ele autentica o usuário. Se nenhum token for encontrado, ele simplesmente passa
 * a requisição para o próximo filtro, permitindo que o Spring Security aplique
 * as regras de 'permitAll' ou de autenticação.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MessageUtil messageUtil;
    private final TokenValidationUtil tokenValidationUtil;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLES_CLAIM = "roles";
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String ROLES_ATTRIBUTE = "roles";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String token = extractTokenFromRequest(request);

        // Se não houver token, passa para o próximo filtro. Spring Security decidirá se a rota é pública.
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            log.debug("Token found, attempting to validate and authenticate...");

            if (validateToken(token, response)) {
                authenticateUser(token, request);
            }
            // Se a validação falhar, a resposta de erro já foi enviada e o método validateToken retorna false.
            // Não devemos continuar a cadeia de filtros se o token for inválido.

        } catch (Exception e) {
            log.error("An unexpected error occurred during JWT processing", e);
            sendUnauthorizedResponse(response, messageUtil.getMessage("config.jwt.token.validation.error"));
            return; // Garante que não continuaremos a cadeia em caso de exceção
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do header Authorization.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(7);
        }
        
        return null;
    }

    /**
     * Valida o token JWT e suas propriedades.
     */
    private boolean validateToken(String token, HttpServletResponse response) throws IOException {
        String username = jwtService.extractUsername(token);
        if (username == null) {
            log.warn("Token sem username válido: {}", maskToken(token));
            sendUnauthorizedResponse(response, messageUtil.getMessage("config.jwt.token.no.username"));
            return false;
        }

        // Usar TokenValidationUtil para validação básica
        if (!tokenValidationUtil.isTokenValid(token, username)) {
            log.warn("Token inválido recebido: {}", maskToken(token));
            sendUnauthorizedResponse(response, messageUtil.getMessage("config.jwt.token.invalid"));
            return false;
        }

        // Verificar se é um access token
        if (!tokenValidationUtil.isAccessToken(token)) {
            log.warn("Tipo de token inválido para autenticação");
            sendUnauthorizedResponse(response, messageUtil.getMessage("config.jwt.token.type.invalid"));
            return false;
        }

        return true;
    }

    /**
     * Autentica o usuário baseado no token JWT.
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        String username = jwtService.extractUsername(token);
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String userIdStr = jwtService.extractUserId(token);
            if (userIdStr == null) {
                log.warn("Token sem userId válido: {}", maskToken(token));
                return;
            }
            
            UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                log.warn("UserId inválido no token: {}", maskToken(token));
                return;
            }

            List<String> roles = extractRolesFromToken(token);
            List<SimpleGrantedAuthority> authorities = createAuthorities(roles);
            
            UserDetails userDetails = createUserDetails(username, authorities);
            UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(userDetails, authorities, request);
            
            setRequestAttributes(request, userId, username, roles);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    /**
     * Extrai as roles do token JWT.
     */
    private List<String> extractRolesFromToken(String token) {
        return jwtService.extractClaim(token, claims -> {
            Object rolesObj = claims.get(ROLES_CLAIM);
            if (rolesObj instanceof List<?> rolesList) {
                return rolesList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
            }
            return List.of();
        });
    }

    /**
     * Cria as autoridades baseadas nas roles.
     */
    private List<SimpleGrantedAuthority> createAuthorities(List<String> roles) {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
            .toList();
    }

    /**
     * Cria os detalhes do usuário para autenticação.
     */
    private UserDetails createUserDetails(String username, List<SimpleGrantedAuthority> authorities) {
        return User.builder()
            .username(username)
            .password("") // Não precisamos da senha aqui
            .authorities(authorities)
            .build();
    }

    /**
     * Cria o token de autenticação.
     */
    private UsernamePasswordAuthenticationToken createAuthenticationToken(
            UserDetails userDetails, List<SimpleGrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }

    /**
     * Define os atributos da requisição.
     */
    private void setRequestAttributes(HttpServletRequest request, UUID userId, String username, List<String> roles) {
        request.setAttribute(USER_ID_ATTRIBUTE, userId);
        request.setAttribute(USERNAME_ATTRIBUTE, username);
        request.setAttribute(ROLES_ATTRIBUTE, roles);
    }

    /**
     * Mascara o token para logs de segurança.
     */
    private String maskToken(String token) {
        return tokenValidationUtil.maskToken(token);
    }

    /**
     * Envia resposta de não autorizado.
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"timestamp\": %d}",
            message, System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
    }


}
