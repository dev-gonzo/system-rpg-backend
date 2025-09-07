package br.com.systemrpg.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import br.com.systemrpg.backend.util.ResponseUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.systemrpg.backend.dto.AuthResponse;
import br.com.systemrpg.backend.dto.LoginRequest;
import br.com.systemrpg.backend.dto.RefreshTokenRequest;
import br.com.systemrpg.backend.dto.JwksResponse;
import br.com.systemrpg.backend.dto.TokenIntrospectRequest;
import br.com.systemrpg.backend.dto.TokenIntrospectResponse;
import br.com.systemrpg.backend.dto.hateoas.AuthHateoasResponse;
import br.com.systemrpg.backend.dto.response.ErrorResponse;
import br.com.systemrpg.backend.dto.response.SuccessResponse;
import br.com.systemrpg.backend.hateoas.HateoasLinkBuilder;
import br.com.systemrpg.backend.mapper.AuthHateoasMapper;
import br.com.systemrpg.backend.service.AuthService;
import br.com.systemrpg.backend.util.MessageUtil;
import br.com.systemrpg.backend.util.ValidationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável pelos endpoints de autenticação.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
public class AuthController {

    private final AuthService authService;
    private final AuthHateoasMapper authHateoasMapper;
    private final HateoasLinkBuilder hateoasLinkBuilder;
    private final MessageUtil messageUtil;

    /**
     * Endpoint para login de usuário.
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna tokens de acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuário inativo")
    })
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            AuthHateoasResponse hateoasResponse = authHateoasMapper.toHateoasResponse(response);
            
            // Adicionar links HATEOAS baseados nas permissões
            hateoasLinkBuilder.addAuthLinks(hateoasResponse);

            return ResponseEntity.ok(hateoasResponse);

        } catch (Exception e) {
            return (ResponseEntity<Object>) (ResponseEntity<?>) ResponseUtil.unauthorized(
                    messageUtil.getMessage("controller.auth.login.error"), 
                    e.getMessage());
        }
    }

    /**
     * Endpoint para renovar access token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renova o access token usando o refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido"),
            @ApiResponse(responseCode = "401", description = "Refresh token expirado ou inválido")
    })
    public ResponseEntity<Object> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            AuthHateoasResponse hateoasResponse = authHateoasMapper.toHateoasResponse(response);
            
            // Adicionar links HATEOAS baseados nas permissões
            hateoasLinkBuilder.addAuthLinks(hateoasResponse);
            
            return ResponseEntity.ok(hateoasResponse);

        } catch (Exception e) {
            return (ResponseEntity<Object>) (ResponseEntity<?>) ResponseUtil.unauthorized(
                    messageUtil.getMessage("controller.auth.refresh.error"), 
                    e.getMessage());
        }
    }

    /**
     * Endpoint para logout de usuário.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout de usuário", description = "Invalida os tokens do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<Object> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            if (!ValidationUtil.isValidAuthHeader(authHeader)) {
                ErrorResponse errorResponse = new ErrorResponse(
                    messageUtil.getMessage("controller.auth.logout.error"), 
                    "Authorization header is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            authService.logout(authHeader);

            SuccessResponse<String> response = new SuccessResponse<>(
                    messageUtil.getMessage("controller.auth.logout.success")
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    messageUtil.getMessage("controller.auth.logout.error"), 
                    e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Endpoint para introspecção de token.
     */
    @PostMapping("/introspect")
    @Operation(summary = "Introspecção de token", description = "Valida um token JWT e retorna suas claims")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido - retorna claims"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<TokenIntrospectResponse> introspect(@Valid @RequestBody TokenIntrospectRequest request) {
        log.info("Recebida requisição de introspecção de token");
        
        try {
            TokenIntrospectResponse response = authService.introspect(request);
            
            if (response.getActive()) {
                log.info("Token válido - introspecção bem-sucedida");
                return ResponseUtil.ok(response);
            } else {
                log.warn("Token inválido - {}", response.getError());
                return ResponseUtil.status(HttpStatus.UNAUTHORIZED, response);
            }
            
        } catch (Exception e) {
            log.error("Erro durante introspecção do token: {}", e.getMessage());
            TokenIntrospectResponse errorResponse = TokenIntrospectResponse.inactive(
                messageUtil.getMessage("controller.auth.introspect.error")
            );
            return ResponseUtil.status(HttpStatus.UNAUTHORIZED, errorResponse);
        }
    }
    
    /**
     * Endpoint para JWKS (JSON Web Key Set).
     * Retorna as chaves públicas para validação de tokens JWT.
     */
    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "JWKS - JSON Web Key Set", description = "Retorna as chaves públicas para validação de tokens JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWKS retornado com sucesso")
    })
    public ResponseEntity<JwksResponse> jwks() {
        try {
            log.info("Solicitação de JWKS recebida");
            JwksResponse jwksResponse = authService.generateJwks();
            log.info("JWKS gerado com {} chaves", jwksResponse.getKeys().size());
            return ResponseEntity.ok(jwksResponse);
            
        } catch (Exception e) {
            log.error("Erro ao gerar JWKS: {}", e.getMessage());
            // Retorna status 500 em caso de erro interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JwksResponse.of(List.of()));
        }
    }

}
