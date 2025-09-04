package br.com.systemrpg.backend.hateoas;

import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.util.AuthorizationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.UUID;

import static br.com.systemrpg.backend.util.AuthorizationUtil.*;

/**
 * Builder responsável por construir links HATEOAS com base nas permissões do usuário.
 */
@Component
public class HateoasLinkBuilder {
    
    @Value("${app.base-url:http://localhost:8080}")
    private String defaultBaseUrl;
    
    @Value("${app.api.v1.path:/auth/api/v1}")
    private String apiV1Path;
    
    private static final String USERS_PATH = "/users";
    private static final String REFRESH_PATH = "/refresh";
    private static final String LOGOUT_PATH = "/logout";
    private static final String REGISTER_PATH = "/register";
    private static final String STATUS_PATH = "/status";
    private static final String VERIFY_EMAIL_PATH = "/verify-email";
    private static final String SEARCH_PATH = "/search";
    
    // Link relations
    private static final String SELF_REL = "self";
    private static final String REFRESH_REL = "refresh";
    private static final String LOGOUT_REL = "logout";
    private static final String USERS_REL = "users";
    private static final String CREATE_USER_REL = "create-user";
    private static final String UPDATE_REL = "update";
    private static final String TOGGLE_STATUS_REL = "toggle-status";
    private static final String VERIFY_EMAIL_REL = "verify-email";
    private static final String SEARCH_USERS_REL = "search-users";
    private static final String DELETE_REL = "delete";
    private static final String FIRST_REL = "first";
    private static final String PREV_REL = "prev";
    private static final String NEXT_REL = "next";
    private static final String LAST_REL = "last";
    
    // Query parameters
    private static final String PAGE_PARAM = "?page=";
    private static final String SIZE_PARAM = "&size=";
    
    // HTTP Methods
    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String PUT_METHOD = "PUT";
    private static final String PATCH_METHOD = "PATCH";
    private static final String DELETE_METHOD = "DELETE";
    
    // Roles - usando constantes da AuthorizationUtil
    
    /**
     * Adiciona links relacionados ao usuário baseado nas permissões.
     */
    public void addUserLinks(HateoasResponse response, User user) {
        addUserLinks(response, user.getId());
    }
    
    /**
     * Adiciona links individuais para objetos de usuário em listas (sem links globais).
     */
    public void addIndividualUserLinks(HateoasResponse response, User user) {
        addIndividualUserLinks(response, user.getId());
    }
    
    /**
     * Adiciona links individuais para objetos de usuário em listas (sem links globais).
     */
    public void addIndividualUserLinks(HateoasResponse response, UUID userId) {
        if (!isAuthenticated()) {
            return;
        }
        
        String baseUrl = getBaseUrl();
        Collection<String> authorities = getCurrentUserAuthorities();
        
        // Link self - sempre disponível se o usuário pode ver o recurso
        response.addLink(baseUrl + apiV1Path + USERS_PATH + "/" + userId, SELF_REL, GET_METHOD);
        
        // Links baseados em permissões (apenas individuais, sem links globais)
        String apiBaseUrl = baseUrl + apiV1Path;
        if (hasAdminRole(authorities)) {
            addAllIndividualUserLinks(response, apiBaseUrl, userId);
        } else if (hasManagerRole(authorities)) {
            addManagerIndividualUserLinks(response, apiBaseUrl, userId);
        } else {
            addSelfUserLinks(response, apiBaseUrl, userId);
        }
    }
    
    /**
     * Adiciona links relacionados ao usuário baseado nas permissões.
     */
    public void addUserLinks(HateoasResponse response, UUID userId) {
        if (!isAuthenticated()) {
            return;
        }
        
        String baseUrl = getBaseUrl();
        Collection<String> authorities = getCurrentUserAuthorities();
        
        // Link self - sempre disponível se o usuário pode ver o recurso
        response.addLink(baseUrl + apiV1Path + USERS_PATH + "/" + userId, SELF_REL, GET_METHOD);
        
        // Links baseados em permissões
        String apiBaseUrl = baseUrl + apiV1Path;
        if (hasAdminRole(authorities)) {
            addAllUserLinks(response, apiBaseUrl, userId);
        } else if (hasManagerRole(authorities)) {
            addManagerUserLinks(response, apiBaseUrl, userId);
        } else {
            addSelfUserLinks(response, apiBaseUrl, userId);
        }
    }
    
    /**
     * Adiciona links de autenticação.
     */
    public void addAuthLinks(HateoasResponse response) {
        String baseUrl = getBaseUrl() + apiV1Path;
        
        // Link para refresh token
        response.addLink(baseUrl + REFRESH_PATH, REFRESH_REL, POST_METHOD);
        
        // Link para logout
        response.addLink(baseUrl + LOGOUT_PATH, LOGOUT_REL, POST_METHOD);
    }
    
    /**
     * Adiciona links de paginação.
     */
    public void addPaginationLinks(PagedHateoasResponse<?> response, Pageable pageable, String basePath, String queryParams) {
        String baseUrl = getBaseUrl() + apiV1Path + basePath;
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        
        String queryString = queryParams != null ? "&" + queryParams : "";

        // Self link
        response.addLink(baseUrl + PAGE_PARAM + currentPage + SIZE_PARAM + pageSize + queryString, SELF_REL, GET_METHOD);
        
        // First page
        if (currentPage > 0) {
            response.addLink(baseUrl + PAGE_PARAM + "0" + SIZE_PARAM + pageSize + queryString, FIRST_REL, GET_METHOD);
        }
        
        // Previous page
        if (currentPage > 0) {
            response.addLink(baseUrl + PAGE_PARAM + (currentPage - 1) + SIZE_PARAM + pageSize + queryString, PREV_REL, GET_METHOD);
        }
        
        // Next page - precisamos do total de páginas do PageInfo
        if (response.getPage() != null && currentPage < response.getPage().getTotalPages() - 1) {
            response.addLink(baseUrl + PAGE_PARAM + (currentPage + 1) + SIZE_PARAM + pageSize + queryString, NEXT_REL, GET_METHOD);
        }
        
        // Last page
        if (response.getPage() != null && currentPage < response.getPage().getTotalPages() - 1) {
            response.addLink(baseUrl + PAGE_PARAM + (response.getPage().getTotalPages() - 1) + SIZE_PARAM + pageSize + queryString, LAST_REL, GET_METHOD);
        }
    }
    
    /**
     * Adiciona todos os links para ROLE_ADMIN.
     */
    private void addAllUserLinks(HateoasResponse response, String baseUrl, UUID userId) {
        response.addLink(baseUrl + USERS_PATH, USERS_REL, GET_METHOD);
        response.addLink(baseUrl + USERS_PATH + REGISTER_PATH, CREATE_USER_REL, POST_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId, UPDATE_REL, PUT_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId + STATUS_PATH, TOGGLE_STATUS_REL, PATCH_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId + VERIFY_EMAIL_PATH, VERIFY_EMAIL_REL, PATCH_METHOD);
        response.addLink(baseUrl + USERS_PATH + SEARCH_PATH, SEARCH_USERS_REL, GET_METHOD);
    }
    
    /**
     * Adiciona links individuais para ROLE_ADMIN (sem links globais).
     */
    private void addAllIndividualUserLinks(HateoasResponse response, String baseUrl, UUID userId) {
        response.addLink(baseUrl + USERS_PATH + "/" + userId, UPDATE_REL, PUT_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId + STATUS_PATH, TOGGLE_STATUS_REL, PATCH_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId + VERIFY_EMAIL_PATH, VERIFY_EMAIL_REL, PATCH_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId, DELETE_REL, DELETE_METHOD);
    }
    
    /**
     * Adiciona links para ROLE_MANAGER.
     */
    private void addManagerUserLinks(HateoasResponse response, String baseUrl, UUID userId) {
        response.addLink(baseUrl + USERS_PATH, USERS_REL, GET_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId, UPDATE_REL, PUT_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId + STATUS_PATH, TOGGLE_STATUS_REL, PATCH_METHOD);
        response.addLink(baseUrl + USERS_PATH + SEARCH_PATH, SEARCH_USERS_REL, GET_METHOD);
    }
    
    /**
     * Adiciona links individuais para gerentes (sem links globais).
     */
    private void addManagerIndividualUserLinks(HateoasResponse response, String baseUrl, UUID userId) {
        response.addLink(baseUrl + USERS_PATH + "/" + userId, UPDATE_REL, PUT_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId + STATUS_PATH, TOGGLE_STATUS_REL, PATCH_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId, DELETE_REL, DELETE_METHOD);
    }
    
    /**
     * Adiciona links para o próprio usuário.
     */
    private void addSelfUserLinks(HateoasResponse response, String baseUrl, UUID userId) {
        // Usuário pode atualizar seus próprios dados
        response.addLink(baseUrl + USERS_PATH + "/" + userId, UPDATE_REL, PUT_METHOD);
        response.addLink(baseUrl + USERS_PATH + "/" + userId + VERIFY_EMAIL_PATH, VERIFY_EMAIL_REL, PATCH_METHOD);
    }
    

    
    /**
     * Adiciona links gerais para listas de usuários.
     */
    public void addUserLinks(PagedHateoasResponse<?> response) {
        if (!isAuthenticated()) {
            return;
        }
        
        String baseUrl = getBaseUrl() + apiV1Path;
        Collection<String> authorities = getCurrentUserAuthorities();
        
        // Links baseados em permissões
        if (hasAdminRole(authorities)) {
            response.addLink(baseUrl + USERS_PATH + REGISTER_PATH, CREATE_USER_REL, POST_METHOD);
            response.addLink(baseUrl + USERS_PATH + SEARCH_PATH, SEARCH_USERS_REL, GET_METHOD);
        } else if (hasManagerRole(authorities)) {
            response.addLink(baseUrl + USERS_PATH + SEARCH_PATH, SEARCH_USERS_REL, GET_METHOD);
        }
    }
    
    // Métodos de verificação de roles removidos - usando AuthorizationUtil
    
    /**
     * Obtém a URL base da aplicação.
     */
    private String getBaseUrl() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        } catch (IllegalStateException e) {
            // Retorna URL padrão quando não há contexto de request
            return defaultBaseUrl;
        }
    }
}
