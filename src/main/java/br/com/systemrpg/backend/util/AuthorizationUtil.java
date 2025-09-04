package br.com.systemrpg.backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utilitário para centralizar lógicas de autorização e permissões.
 * Reduz a duplicação de código ao verificar roles e permissões.
 */
@Slf4j
public class AuthorizationUtil {

    /**
     * Construtor privado para prevenir instanciação.
     */
    private AuthorizationUtil() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não deve ser instanciada");
    }

    // Constantes de roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String MANAGER = "MANAGER";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String USER = "USER";

    /**
     * Obtém a autenticação atual do contexto de segurança.
     *
     * @return a autenticação atual ou null se não autenticado
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Verifica se há um usuário autenticado.
     *
     * @return true se há usuário autenticado, false caso contrário
     */
    public static boolean isAuthenticated() {
        Authentication auth = getCurrentAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Obtém as autoridades do usuário autenticado.
     *
     * @return coleção de autoridades ou coleção vazia se não autenticado
     */
    public static Collection<String> getCurrentUserAuthorities() {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) {
            return Collections.emptyList();
        }
        
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
    }

    /**
     * Obtém as autoridades de uma autenticação específica.
     *
     * @param auth a autenticação
     * @return coleção de autoridades ou coleção vazia se auth for null
     */
    public static Collection<String> getAuthorities(Authentication auth) {
        if (auth == null) {
            return Collections.emptyList();
        }
        
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
    }

    /**
     * Verifica se o usuário atual tem role de ADMIN.
     *
     * @return true se tem role de admin, false caso contrário
     */
    public static boolean hasAdminRole() {
        return hasAdminRole(getCurrentUserAuthorities());
    }

    /**
     * Verifica se as autoridades incluem role de ADMIN.
     *
     * @param authorities as autoridades a verificar
     * @return true se tem role de admin, false caso contrário
     */
    public static boolean hasAdminRole(Collection<String> authorities) {
        return authorities.stream().anyMatch(auth -> 
            ROLE_ADMIN.equals(auth) || ADMIN.equals(auth));
    }

    /**
     * Verifica se o usuário atual tem role de MANAGER.
     *
     * @return true se tem role de manager, false caso contrário
     */
    public static boolean hasManagerRole() {
        return hasManagerRole(getCurrentUserAuthorities());
    }

    /**
     * Verifica se as autoridades incluem role de MANAGER.
     *
     * @param authorities as autoridades a verificar
     * @return true se tem role de manager, false caso contrário
     */
    public static boolean hasManagerRole(Collection<String> authorities) {
        return authorities.stream().anyMatch(auth -> 
            ROLE_MANAGER.equals(auth) || MANAGER.equals(auth));
    }

    /**
     * Verifica se o usuário atual tem role de USER.
     *
     * @return true se tem role de user, false caso contrário
     */
    public static boolean hasUserRole() {
        return hasUserRole(getCurrentUserAuthorities());
    }

    /**
     * Verifica se as autoridades incluem role de USER.
     *
     * @param authorities as autoridades a verificar
     * @return true se tem role de user, false caso contrário
     */
    public static boolean hasUserRole(Collection<String> authorities) {
        return authorities.stream().anyMatch(auth -> 
            ROLE_USER.equals(auth) || USER.equals(auth));
    }

    /**
     * Verifica se o usuário tem qualquer uma das roles especificadas.
     *
     * @param roles as roles a verificar
     * @return true se tem pelo menos uma das roles, false caso contrário
     */
    public static boolean hasAnyRole(String... roles) {
        return hasAnyRole(getCurrentUserAuthorities(), roles);
    }

    /**
     * Verifica se as autoridades incluem qualquer uma das roles especificadas.
     *
     * @param authorities as autoridades a verificar
     * @param roles as roles a verificar
     * @return true se tem pelo menos uma das roles, false caso contrário
     */
    public static boolean hasAnyRole(Collection<String> authorities, String... roles) {
        if (authorities.isEmpty() || roles.length == 0) {
            return false;
        }
        
        List<String> rolesList = List.of(roles);
        return authorities.stream().anyMatch(rolesList::contains);
    }

    /**
     * Verifica se o usuário tem todas as roles especificadas.
     *
     * @param roles as roles a verificar
     * @return true se tem todas as roles, false caso contrário
     */
    public static boolean hasAllRoles(String... roles) {
        return hasAllRoles(getCurrentUserAuthorities(), roles);
    }

    /**
     * Verifica se as autoridades incluem todas as roles especificadas.
     *
     * @param authorities as autoridades a verificar
     * @param roles as roles a verificar
     * @return true se tem todas as roles, false caso contrário
     */
    public static boolean hasAllRoles(Collection<String> authorities, String... roles) {
        if (authorities.isEmpty() || roles.length == 0) {
            return false;
        }
        
        List<String> rolesList = List.of(roles);
        return rolesList.stream().allMatch(authorities::contains);
    }

    /**
     * Obtém o nome do usuário autenticado.
     *
     * @return o nome do usuário ou null se não autenticado
     */
    public static String getCurrentUsername() {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) {
            return null;
        }
        
        Object principal = auth.getPrincipal();
        if (principal instanceof String string) {
            return string;
        }
        
        return principal.toString();
    }

    /**
     * Verifica se o usuário atual é o proprietário do recurso.
     *
     * @param resourceOwnerUsername o username do proprietário do recurso
     * @return true se é o proprietário, false caso contrário
     */
    public static boolean isResourceOwner(String resourceOwnerUsername) {
        String currentUsername = getCurrentUsername();
        return currentUsername != null && currentUsername.equals(resourceOwnerUsername);
    }

    /**
     * Verifica se o usuário pode acessar o recurso (é admin, manager ou proprietário).
     *
     * @param resourceOwnerUsername o username do proprietário do recurso
     * @return true se pode acessar, false caso contrário
     */
    public static boolean canAccessResource(String resourceOwnerUsername) {
        return hasAdminRole() || hasManagerRole() || isResourceOwner(resourceOwnerUsername);
    }
}
