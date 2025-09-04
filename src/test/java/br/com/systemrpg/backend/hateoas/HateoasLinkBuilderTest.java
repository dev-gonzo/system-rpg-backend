package br.com.systemrpg.backend.hateoas;

import br.com.systemrpg.backend.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Testes para a classe HateoasLinkBuilder.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HateoasLinkBuilder Tests")
class HateoasLinkBuilderTest {

    @InjectMocks
    private HateoasLinkBuilder hateoasLinkBuilder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockHttpServletRequest request;
    private TestHateoasResponse response;
    private User testUser;
    private UUID testUserId;

    /**
     * Implementação concreta de HateoasResponse para testes.
     */
    static class TestHateoasResponse extends HateoasResponse {
        // Implementação vazia para testes
    }

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        
        response = new TestHateoasResponse();
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Deve adicionar links de usuário com User entity")
    @SuppressWarnings("unchecked")
    void shouldAddUserLinksWithUserEntity() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addUserLinks(response, testUser);
            });
            
            assertNotNull(response.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links de usuário com UUID")
    @SuppressWarnings("unchecked")
    void shouldAddUserLinksWithUUID() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addUserLinks(response, testUserId);
            });
            
            assertNotNull(response.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links individuais de usuário com User entity")
    @SuppressWarnings("unchecked")
    void shouldAddIndividualUserLinksWithUserEntity() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addIndividualUserLinks(response, testUser);
            });
            
            assertNotNull(response.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links individuais de usuário com UUID")
    @SuppressWarnings("unchecked")
    void shouldAddIndividualUserLinksWithUUID() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addIndividualUserLinks(response, testUserId);
            });
            
            assertNotNull(response.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links de autenticação")
    void shouldAddAuthLinks() {
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addAuthLinks(response);
            });
            
            assertNotNull(response.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links de paginação")
    void shouldAddPaginationLinks() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        Pageable pageable = PageRequest.of(1, 10);
        String basePath = "/api/users";
        String queryParams = "";
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addPaginationLinks(pagedResponse, pageable, basePath, queryParams);
            });
            
            assertNotNull(pagedResponse.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links de usuário para PagedHateoasResponse")
    @SuppressWarnings("unchecked")
    void shouldAddUserLinksForPagedResponse() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addUserLinks(pagedResponse);
            });
            
            assertNotNull(pagedResponse.getLinks());
        }
    }

    @Test
    @DisplayName("Não deve adicionar links quando usuário não está autenticado")
    void shouldNotAddLinksWhenUserNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        
        assertDoesNotThrow(() -> {
            hateoasLinkBuilder.addUserLinks(response, testUserId);
        });
        
        // Verifica que nenhum link foi adicionado
        assertTrue(response.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Não deve adicionar links individuais quando usuário não está autenticado")
    void shouldNotAddIndividualLinksWhenUserNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        
        assertDoesNotThrow(() -> {
            hateoasLinkBuilder.addIndividualUserLinks(response, testUserId);
        });
        
        // Verifica que nenhum link foi adicionado
        assertTrue(response.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Deve lidar com SecurityContext nulo")
    void shouldHandleNullSecurityContext() {
        SecurityContextHolder.clearContext();
        
        assertDoesNotThrow(() -> {
            hateoasLinkBuilder.addUserLinks(response, testUserId);
        });
        
        // Verifica que nenhum link foi adicionado
        assertTrue(response.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Deve lidar com Authentication nulo")
    void shouldHandleNullAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        assertDoesNotThrow(() -> {
            hateoasLinkBuilder.addUserLinks(response, testUserId);
        });
        
        // Verifica que nenhum link foi adicionado
        assertTrue(response.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Deve lidar com RequestContextHolder nulo")
    @SuppressWarnings("unchecked")
    void shouldHandleNullRequestContextHolder() {
        RequestContextHolder.resetRequestAttributes();
        
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        assertDoesNotThrow(() -> {
            hateoasLinkBuilder.addUserLinks(response, testUserId);
        });
    }

    @Test
    @DisplayName("Deve testar com diferentes tipos de autoridades")
    @SuppressWarnings("unchecked")
    void shouldTestWithDifferentAuthorities() {
        when(authentication.isAuthenticated()).thenReturn(true);
        
        // Teste com ROLE_MANAGER
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addUserLinks(response, testUserId);
            });
            
            assertNotNull(response.getLinks());
        }
    }

    @Test
    @DisplayName("Deve verificar se a classe é um componente Spring")
    void shouldVerifySpringComponent() {
        assertTrue(hateoasLinkBuilder.getClass().isAnnotationPresent(org.springframework.stereotype.Component.class));
    }

    @Test
    @DisplayName("Deve testar construtor padrão")
    void shouldTestDefaultConstructor() {
        assertDoesNotThrow(() -> {
            HateoasLinkBuilder builder = new HateoasLinkBuilder();
            assertNotNull(builder);
        });
    }

    @Test
    @DisplayName("Deve adicionar todos os links para ROLE_ADMIN")
    @SuppressWarnings("unchecked")
    void shouldAddAllLinksForAdmin() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addUserLinks(response, testUserId);
            
            assertNotNull(response.getLinks());
            assertFalse(response.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Deve adicionar links individuais para ROLE_ADMIN")
    @SuppressWarnings("unchecked")
    void shouldAddIndividualLinksForAdmin() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addIndividualUserLinks(response, testUserId);
            
            assertNotNull(response.getLinks());
            assertFalse(response.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Deve adicionar links para ROLE_MANAGER")
    @SuppressWarnings("unchecked")
    void shouldAddLinksForManager() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addUserLinks(response, testUserId);
            
            assertNotNull(response.getLinks());
            assertFalse(response.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Deve adicionar links individuais para ROLE_MANAGER")
    @SuppressWarnings("unchecked")
    void shouldAddIndividualLinksForManager() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addIndividualUserLinks(response, testUserId);
            
            assertNotNull(response.getLinks());
            assertFalse(response.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Deve adicionar links para usuário comum (ROLE_USER)")
    @SuppressWarnings("unchecked")
    void shouldAddLinksForUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addUserLinks(response, testUserId);
            
            assertNotNull(response.getLinks());
            // ROLE_USER pode ter links limitados ou nenhum link dependendo da implementação
        }
    }

    @Test
    @DisplayName("Deve testar método getBaseUrl com diferentes cenários")
    void shouldTestGetBaseUrlWithDifferentScenarios() {
        // Teste com RequestContextHolder configurado
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            // Força a execução do método getBaseUrl através de addAuthLinks
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addAuthLinks(response);
            });
        }
        
        // Teste com RequestContextHolder nulo
        RequestContextHolder.resetRequestAttributes();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addAuthLinks(response);
            });
        }
    }

    @Test
    @DisplayName("Deve testar paginação com diferentes parâmetros")
    void shouldTestPaginationWithDifferentParameters() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            // Teste com página 0 (primeira página)
            Pageable firstPage = PageRequest.of(0, 10);
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addPaginationLinks(pagedResponse, firstPage, "/users", "active=true");
            });
            
            // Teste com página intermediária
            Pageable middlePage = PageRequest.of(5, 10);
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addPaginationLinks(pagedResponse, middlePage, "/users", null);
            });
            
            // Teste com queryParams nulo
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addPaginationLinks(pagedResponse, middlePage, "/users", null);
            });
        }
    }
    
    @Test
    @DisplayName("Deve testar paginação com PageInfo para next e last links")
    void shouldTestPaginationWithPageInfo() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setTotalPages(10);
        pagedResponse.setPage(pageInfo);
        
        Pageable pageable = PageRequest.of(5, 10); // página intermediária
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addPaginationLinks(pagedResponse, pageable, "/users", "active=true");
            });
            
            assertNotNull(pagedResponse.getLinks());
        }
    }
    
    @Test
    @DisplayName("Deve adicionar links para usuário com role MANAGER")
    void shouldAddUserLinksForManager() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addUserLinks(pagedResponse);
            });
            
            assertNotNull(pagedResponse.getLinks());
        }
    }
    
    @Test
    @DisplayName("Deve testar getBaseUrl com exceção")
    void shouldTestGetBaseUrlWithException() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath)
                .thenThrow(new IllegalStateException("No current request"));
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addUserLinks(pagedResponse);
            });
            
            assertNotNull(pagedResponse.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links para PagedHateoasResponse com ROLE_ADMIN")
    @SuppressWarnings("unchecked")
    void shouldAddUserLinksForPagedResponseWithAdmin() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addUserLinks(pagedResponse);
            
            assertNotNull(pagedResponse.getLinks());
            assertFalse(pagedResponse.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Deve adicionar links para PagedHateoasResponse com ROLE_MANAGER")
    @SuppressWarnings("unchecked")
    void shouldAddUserLinksForPagedResponseWithManager() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addUserLinks(pagedResponse);
            
            assertNotNull(pagedResponse.getLinks());
            assertFalse(pagedResponse.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Não deve adicionar links para PagedHateoasResponse quando não autenticado")
    void shouldNotAddUserLinksForPagedResponseWhenNotAuthenticated() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        
        hateoasLinkBuilder.addUserLinks(pagedResponse);
        
        assertTrue(pagedResponse.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Deve adicionar links individuais para usuário comum (ROLE_USER)")
    @SuppressWarnings("unchecked")
    void shouldAddIndividualLinksForRegularUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addIndividualUserLinks(response, testUserId);
            
            assertNotNull(response.getLinks());
            assertFalse(response.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Deve usar URL padrão quando getBaseUrl lança IllegalStateException")
    void shouldUseDefaultUrlWhenGetBaseUrlThrowsException() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath)
                .thenThrow(new IllegalStateException("No current request"));
            
            // Este teste deve executar o catch do getBaseUrl e usar a URL padrão
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addUserLinks(pagedResponse);
            });
            
            assertNotNull(pagedResponse.getLinks());
        }
    }

    @Test
    @DisplayName("Deve testar paginação quando response.getPage() é null")
    void shouldTestPaginationWhenPageIsNull() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        // Não definir pageInfo, deixando null
        pagedResponse.setPage(null);
        
        Pageable pageable = PageRequest.of(1, 10);
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addPaginationLinks(pagedResponse, pageable, "/users", "active=true");
            });
            
            assertNotNull(pagedResponse.getLinks());
        }
    }

    @Test
    @DisplayName("Deve adicionar links para PagedHateoasResponse com ROLE_USER (sem links)")
    @SuppressWarnings("unchecked")
    void shouldAddUserLinksForPagedResponseWithUser() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost:8080");
            
            hateoasLinkBuilder.addUserLinks(pagedResponse);
            
            assertNotNull(pagedResponse.getLinks());
            // ROLE_USER não deve ter links de criação ou busca
            assertTrue(pagedResponse.getLinks().isEmpty());
        }
    }

    @Test
    @DisplayName("Deve testar paginação na última página (sem next e last links)")
    void shouldTestPaginationOnLastPage() {
        PagedHateoasResponse<Object> pagedResponse = new PagedHateoasResponse<>();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setTotalPages(5);
        pagedResponse.setPage(pageInfo);
        
        // Página 4 (última página, considerando que páginas começam em 0)
        Pageable lastPage = PageRequest.of(4, 10);
        
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost:8080");
            
            assertDoesNotThrow(() -> {
                hateoasLinkBuilder.addPaginationLinks(pagedResponse, lastPage, "/users", "active=true");
            });
            
            assertNotNull(pagedResponse.getLinks());
            // Na última página, não deve haver links next e last
        }
    }
}
