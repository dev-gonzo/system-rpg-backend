package br.com.systemrpg.backend.controller;

import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.UserCreateRequest;
import br.com.systemrpg.backend.dto.UserUpdateRequest;
import br.com.systemrpg.backend.dto.hateoas.UserHateoasResponse;
import br.com.systemrpg.backend.dto.response.AvailabilityResponse;
import br.com.systemrpg.backend.dto.response.ResponseApi;
import br.com.systemrpg.backend.dto.response.UserResponse;
import br.com.systemrpg.backend.hateoas.HateoasLinkBuilder;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import br.com.systemrpg.backend.mapper.UserHateoasMapper;
import br.com.systemrpg.backend.mapper.UserMapper;
import br.com.systemrpg.backend.service.UserService;
import br.com.systemrpg.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller para gerenciamento de usuários.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Management", description = "Endpoints para gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserHateoasMapper userHateoasMapper;
    private final HateoasLinkBuilder hateoasLinkBuilder;
    private final MessageSource messageSource;

    /**
     * Lista todos os usuários com paginação.
     */
    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<PagedHateoasResponse<UserHateoasResponse>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Filtrar apenas usuários ativos")
            @RequestParam(required = false) Boolean active) {

        Page<User> users = getUsersPage(pageable, active);
        PagedHateoasResponse<UserHateoasResponse> hateoasResponse = buildUserListResponse(users, pageable, active);

        return ResponseUtil.okWithSuccess(
                hateoasResponse,
                messageSource.getMessage("controller.user.list.success", null, LocaleContextHolder.getLocale())
        );
    }

    /**
     * Obtém a página de usuários baseada nos filtros.
     */
    private Page<User> getUsersPage(Pageable pageable, Boolean active) {
        if (active != null && active) {
            return userService.findActiveUsers(pageable);
        } else {
            return userService.findAll(pageable);
        }
    }

    /**
     * Constrói a resposta HATEOAS para lista de usuários.
     */
    private PagedHateoasResponse<UserHateoasResponse> buildUserListResponse(Page<User> users, Pageable pageable, Boolean active) {
        Page<UserResponse> userResponses = users.map(userMapper::toResponse);
        PagedHateoasResponse<UserHateoasResponse> hateoasResponse = userHateoasMapper.toPagedHateoasResponse(userResponses);
        
        addIndividualUserLinks(hateoasResponse, users);
        addCollectionLinks(hateoasResponse, pageable, active);
        
        return hateoasResponse;
    }

    /**
     * Adiciona links HATEOAS individuais para cada usuário.
     */
    private void addIndividualUserLinks(PagedHateoasResponse<UserHateoasResponse> hateoasResponse, Page<User> users) {
        for (int i = 0; i < hateoasResponse.getContent().size(); i++) {
            UserHateoasResponse userHateoas = hateoasResponse.getContent().get(i);
            User originalUser = users.getContent().get(i);
            hateoasLinkBuilder.addIndividualUserLinks(userHateoas, originalUser);
        }
    }

    /**
     * Adiciona links HATEOAS da coleção e paginação.
     */
    private void addCollectionLinks(PagedHateoasResponse<UserHateoasResponse> hateoasResponse, Pageable pageable, Boolean active) {
        hateoasLinkBuilder.addUserLinks(hateoasResponse);
        hateoasLinkBuilder.addPaginationLinks(hateoasResponse, pageable, "/users", active != null ? "active=" + active : null);
    }

    /**
     * Busca um usuário por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @userService.findById(#id).username == authentication.name")
    @Operation(summary = "Buscar usuário por ID", description = "Busca um usuário específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<UserHateoasResponse>> getUserById(
            @Parameter(description = "ID do usuário") @PathVariable UUID id) {

        User user = userService.findById(id);
        UserResponse userResponse = userMapper.toResponse(user);
        UserHateoasResponse hateoasResponse = userHateoasMapper.toHateoasResponse(userResponse);
        
        // Adicionar links HATEOAS baseados nas permissões
        hateoasLinkBuilder.addUserLinks(hateoasResponse, user);

        return ResponseUtil.okWithSuccess(
                hateoasResponse,
                messageSource.getMessage("controller.user.found.success", null, LocaleContextHolder.getLocale())
        );
    }



    /**
     * Cria um novo usuário.
     */
    @PostMapping("/register")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Username ou email já existem"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<UserHateoasResponse>> createUser(
            @Parameter(description = "Dados do usuário a ser criado")
            @Valid @RequestBody UserCreateRequest request) {

        User user = userMapper.toEntity(request);
        User createdUser = userService.createUser(user, request.getRoles());
        UserResponse userResponse = userMapper.toResponse(createdUser);
        UserHateoasResponse hateoasResponse = userHateoasMapper.toHateoasResponse(userResponse);
        
        // Adicionar links HATEOAS baseados nas permissões
        hateoasLinkBuilder.addUserLinks(hateoasResponse, createdUser);

        return ResponseUtil.createdWithSuccess(
                hateoasResponse,
                messageSource.getMessage("controller.user.created.success", null, LocaleContextHolder.getLocale())
        );
    }

    /**
     * Atualiza um usuário existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @userService.findById(#id).username != 'admin') or @userService.findById(#id).username == authentication.name")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Username ou email já existem"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<UserHateoasResponse>> updateUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID id,
            @Parameter(description = "Dados atualizados do usuário")
            @Valid @RequestBody UserUpdateRequest request) {

        User user = userMapper.toEntity(request);
        User updatedUser = userService.updateUser(id, user, request.getRoles());
        UserResponse userResponse = userMapper.toResponse(updatedUser);
        UserHateoasResponse hateoasResponse = userHateoasMapper.toHateoasResponse(userResponse);
        
        // Adicionar links HATEOAS baseados nas permissões
        hateoasLinkBuilder.addUserLinks(hateoasResponse, updatedUser);

        return ResponseUtil.okWithSuccess(
                hateoasResponse,
                messageSource.getMessage("controller.user.updated.success", null, LocaleContextHolder.getLocale())
        );
    }

    /**
     * Ativa ou desativa um usuário.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @userService.findById(#id).username != 'admin')")
    @Operation(summary = "Alterar status do usuário", description = "Ativa ou desativa um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<UserResponse>> toggleUserStatus(
            @Parameter(description = "ID do usuário") @PathVariable UUID id,
            @Parameter(description = "Novo status (true = ativo, false = inativo)")
            @RequestParam Boolean active) {

        User user = userService.toggleUserStatus(id, active);
        UserResponse userResponse = userMapper.toResponse(user);

        return ResponseUtil.okWithSuccess(
                userResponse,
                messageSource.getMessage("controller.user.status.changed.success", null, LocaleContextHolder.getLocale())
        );
    }

    /**
     * Desativa um usuário (soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @userService.findById(#id).username != 'admin')")
    @Operation(summary = "Desativar usuário", description = "Desativa um usuário (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<String>> deactivateUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID id) {

        userService.deactivateUser(id);

        return ResponseUtil.okWithSuccess(
                "Usuário desativado",
                messageSource.getMessage("controller.user.deactivated.success", null, LocaleContextHolder.getLocale())
        );
    }

    /**
     * Exclui permanentemente um usuário.
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir usuário permanentemente", description = "Exclui permanentemente um usuário do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário excluído permanentemente"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<String>> deleteUserPermanently(
            @Parameter(description = "ID do usuário") @PathVariable UUID id) {

        userService.deleteUser(id);

        return ResponseUtil.okWithSuccess(
                "Usuário excluído",
                messageSource.getMessage("controller.user.deleted.success", null, LocaleContextHolder.getLocale())
        );
    }

    /**
     * Verifica disponibilidade de username.
     */
    @GetMapping("/check-username/{username}")
    @Operation(summary = "Verificar disponibilidade de username", description = "Verifica se um username está disponível")
    @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
    public ResponseEntity<ResponseApi<AvailabilityResponse>> checkUsernameAvailability(
            @Parameter(description = "Username a ser verificado") @PathVariable String username) {

        boolean available = userService.isUsernameAvailable(username);
        String messageKey = available ? "controller.user.username.available" : "controller.user.username.unavailable";
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

        AvailabilityResponse availabilityResponse = new AvailabilityResponse(available);

        return ResponseUtil.okWithSuccess(
                availabilityResponse,
                message
        );
    }

    /**
     * Verifica disponibilidade de email.
     */
    @GetMapping("/check-email/{email}")
    @Operation(summary = "Verificar disponibilidade de email", description = "Verifica se um email está disponível")
    @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
    public ResponseEntity<ResponseApi<AvailabilityResponse>> checkEmailAvailability(
            @Parameter(description = "Email a ser verificado") @PathVariable String email) {

        boolean available = userService.isEmailAvailable(email);
        String messageKey = available ? "controller.user.email.available" : "controller.user.email.unavailable";
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

        AvailabilityResponse availabilityResponse = new AvailabilityResponse(available);

        return ResponseUtil.okWithSuccess(
                availabilityResponse,
                message
        );
    }

    /**
     * Verifica email de um usuário.
     */
    @PatchMapping("/{id}/verify-email")
    @PreAuthorize("hasRole('ADMIN') or @userService.findById(#id).username == authentication.name")
    @Operation(summary = "Verificar email do usuário", description = "Marca o email de um usuário como verificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verificado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ResponseApi<UserResponse>> verifyEmail(
            @Parameter(description = "ID do usuário") @PathVariable UUID id) {

        User user = userService.verifyEmail(id);
        UserResponse userResponse = userMapper.toResponse(user);

        return ResponseUtil.okWithSuccess(
                userResponse,
                messageSource.getMessage("controller.user.email.verified.success", null, LocaleContextHolder.getLocale())
        );
    }
}
