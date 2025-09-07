package br.com.systemrpg.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.systemrpg.backend.config.TestSecurityConfig;
import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.UserCreateRequest;
import br.com.systemrpg.backend.dto.response.RoleResponse;
import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.TokenValidationUtil;
import br.com.systemrpg.backend.dto.response.UserResponse;
import br.com.systemrpg.backend.dto.UserUpdateRequest;
import br.com.systemrpg.backend.dto.hateoas.UserHateoasResponse;

import br.com.systemrpg.backend.exception.BusinessException;
import br.com.systemrpg.backend.hateoas.HateoasLinkBuilder;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import br.com.systemrpg.backend.hateoas.PageInfo;
import br.com.systemrpg.backend.mapper.UserHateoasMapper;
import br.com.systemrpg.backend.mapper.UserMapper;
import br.com.systemrpg.backend.service.JwtService;
import br.com.systemrpg.backend.service.UserService;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "spring.security.enabled=false",
    "management.security.enabled=false"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserHateoasMapper userHateoasMapper;

    @MockitoBean
    private HateoasLinkBuilder hateoasLinkBuilder;

    @MockitoBean
    private MessageSource messageSource;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private MessageUtil messageUtil;

    @MockitoBean
    private TokenValidationUtil tokenValidationUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;
    private UserResponse userResponse;
    private UserHateoasResponse userHateoasResponse;
    private UserCreateRequest userCreateRequest;
    private UserUpdateRequest userUpdateRequest;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("USER");
        testRole.setIsActive(true);

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(false);
        testUser.setRoles(Set.of(testRole));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setId(testRole.getId());
        roleResponse.setName("USER");
        roleResponse.setIsActive(true);
        
        userResponse = UserResponse.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .isEmailVerified(false)
                .roles(Set.of(roleResponse))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userHateoasResponse = UserHateoasResponse.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .isActive(true)
                .isEmailVerified(false)
                .roles(List.of(roleResponse))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUsername("newuser");
        userCreateRequest.setEmail("newuser@example.com");
        userCreateRequest.setFirstName("New");
        userCreateRequest.setLastName("User");
        userCreateRequest.setPassword("Password123!");
        userCreateRequest.setRoles(Set.of("USER"));

        userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setUsername("updateduser");
        userUpdateRequest.setFirstName("Updated");
        userUpdateRequest.setLastName("User");
        userUpdateRequest.setEmail("updated@example.com");
        userUpdateRequest.setRoles(Set.of("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithoutFilter_ShouldReturnPagedUsers() throws Exception {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 20), 1);
        PageInfo pageInfo = PageInfo.builder()
                .number(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(1)
                .build();
        
        PagedHateoasResponse<UserHateoasResponse> pagedResponse = PagedHateoasResponse.<UserHateoasResponse>builder()
                .content(List.of(userHateoasResponse))
                .page(pageInfo)
                .build();

        when(userService.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        when(userHateoasMapper.toPagedHateoasResponse(any(Page.class))).thenReturn(pagedResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Success message");

        // Act & Assert
        mockMvc.perform(get("/users")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success message"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page.totalElements").value(1))
                .andExpect(jsonPath("$.data.page.totalPages").value(1));

        verify(userService).findAll(any(Pageable.class));
        verify(userHateoasMapper).toPagedHateoasResponse(any(Page.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithActiveFilter_ShouldReturnFilteredUsers() throws Exception {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 20), 1);
        PageInfo pageInfo = PageInfo.builder()
                .number(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(1)
                .build();
        
        PagedHateoasResponse<UserHateoasResponse> pagedResponse = PagedHateoasResponse.<UserHateoasResponse>builder()
                .content(List.of(userHateoasResponse))
                .page(pageInfo)
                .build();

        when(userService.findActiveUsers(any(Pageable.class))).thenReturn(userPage);
        when(userHateoasMapper.toPagedHateoasResponse(any(Page.class))).thenReturn(pagedResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Success message");

        // Act & Assert
        mockMvc.perform(get("/users")
                .param("active", "true")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success message"));

        verify(userService).findActiveUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithInactiveFilter_ShouldReturnAllUsers() throws Exception {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 20), 1);
        PageInfo pageInfo = PageInfo.builder()
                .number(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(1)
                .build();
        
        PagedHateoasResponse<UserHateoasResponse> pagedResponse = PagedHateoasResponse.<UserHateoasResponse>builder()
                .content(List.of(userHateoasResponse))
                .page(pageInfo)
                .build();

        when(userService.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userHateoasMapper.toPagedHateoasResponse(any(Page.class))).thenReturn(pagedResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Success message");

        // Act & Assert
        mockMvc.perform(get("/users")
                .param("active", "false")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success message"));

        verify(userService).findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithValidId_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.findById(testUserId)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        when(userHateoasMapper.toHateoasResponse(userResponse)).thenReturn(userHateoasResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("User found");

        // Act & Assert
        mockMvc.perform(get("/users/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User found"))
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).findById(testUserId);
        verify(userMapper).toResponse(testUser);
        verify(userHateoasMapper).toHateoasResponse(userResponse);
        verify(hateoasLinkBuilder).addUserLinks(userHateoasResponse, testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(userService.findById(invalidId)).thenThrow(new BusinessException("User not found", HttpStatus.NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/users/{id}", invalidId))
                .andExpect(status().isNotFound());

        verify(userService).findById(invalidId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(testUser);
        when(userService.createUser(any(User.class), anySet())).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        when(userHateoasMapper.toHateoasResponse(userResponse)).thenReturn(userHateoasResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("User created");

        // Act & Assert
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userMapper).toEntity(any(UserCreateRequest.class));
        verify(userService).createUser(any(User.class), anySet());
        verify(userMapper).toResponse(testUser);
        verify(userHateoasMapper).toHateoasResponse(userResponse);
        verify(hateoasLinkBuilder).addUserLinks(userHateoasResponse, testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UserCreateRequest invalidRequest = new UserCreateRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        when(userMapper.toEntity(any(UserUpdateRequest.class))).thenReturn(testUser);
        when(userService.updateUser(eq(testUserId), any(User.class), anySet())).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        when(userHateoasMapper.toHateoasResponse(userResponse)).thenReturn(userHateoasResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("User updated");

        // Act & Assert
        mockMvc.perform(put("/users/{id}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated"));

        verify(userMapper).toEntity(any(UserUpdateRequest.class));
        verify(userService).updateUser(eq(testUserId), any(User.class), anySet());
        verify(userMapper).toResponse(testUser);
        verify(userHateoasMapper).toHateoasResponse(userResponse);
        verify(hateoasLinkBuilder).addUserLinks(userHateoasResponse, testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleUserStatus_WithValidId_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        when(userService.toggleUserStatus(testUserId, false)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Status changed");

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/status", testUserId)
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status changed"));

        verify(userService).toggleUserStatus(testUserId, false);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_WithValidId_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(userService).deactivateUser(testUserId);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("User deactivated");

        // Act & Assert
        mockMvc.perform(delete("/users/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deactivated"));

        verify(userService).deactivateUser(testUserId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserPermanently_WithValidId_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(testUserId);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("User deleted");

        // Act & Assert
        mockMvc.perform(delete("/users/{id}/permanent", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted"));

        verify(userService).deleteUser(testUserId);
    }

    @Test
    void checkUsernameAvailability_WithAvailableUsername_ShouldReturnAvailable() throws Exception {
        // Arrange
        String username = "availableuser";
        when(userService.isUsernameAvailable(username)).thenReturn(true);
        when(messageSource.getMessage(eq("controller.user.username.available"), any(), any()))
                .thenReturn("Username available");

        // Act & Assert
        mockMvc.perform(get("/users/check-username")
                .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Username available"))
                .andExpect(jsonPath("$.data.available").value(true));

        verify(userService).isUsernameAvailable(username);
    }

    @Test
    void checkUsernameAvailability_WithUnavailableUsername_ShouldReturnUnavailable() throws Exception {
        // Arrange
        String username = "takenuser";
        when(userService.isUsernameAvailable(username)).thenReturn(false);
        when(messageSource.getMessage(eq("controller.user.username.unavailable"), any(), any()))
                .thenReturn("Username unavailable");

        // Act & Assert
        mockMvc.perform(get("/users/check-username")
                .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Username unavailable"))
                .andExpect(jsonPath("$.data.available").value(false));

        verify(userService).isUsernameAvailable(username);
    }

    @Test
    void checkEmailAvailability_WithAvailableEmail_ShouldReturnAvailable() throws Exception {
        // Arrange
        String email = "available@example.com";
        when(userService.isEmailAvailable(email)).thenReturn(true);
        when(messageSource.getMessage(eq("controller.user.email.available"), any(), any()))
                .thenReturn("Email available");

        // Act & Assert
        mockMvc.perform(get("/users/check-email")
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email available"))
                .andExpect(jsonPath("$.data.available").value(true));

        verify(userService).isEmailAvailable(email);
    }

    @Test
    void checkEmailAvailability_WithUnavailableEmail_ShouldReturnUnavailable() throws Exception {
        // Arrange
        String email = "taken@example.com";
        when(userService.isEmailAvailable(email)).thenReturn(false);
        when(messageSource.getMessage(eq("controller.user.email.unavailable"), any(), any()))
                .thenReturn("Email unavailable");

        // Act & Assert
        mockMvc.perform(get("/users/check-email")
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email unavailable"))
                .andExpect(jsonPath("$.data.available").value(false));

        verify(userService).isEmailAvailable(email);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verifyEmail_WithValidId_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        when(userService.verifyEmail(testUserId)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Email verified");

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/verify-email", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified"));

        verify(userService).verifyEmail(testUserId);
        verify(userMapper).toResponse(testUser);
    }

    // Testes de autorização removidos para focar na lógica do controller
    // A segurança é testada em testes de integração separados
}
