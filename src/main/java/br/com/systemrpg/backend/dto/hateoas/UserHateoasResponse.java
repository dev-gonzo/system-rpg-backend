package br.com.systemrpg.backend.dto.hateoas;

import br.com.systemrpg.backend.dto.response.RoleResponse;
import br.com.systemrpg.backend.hateoas.HateoasResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO HATEOAS para resposta de usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserHateoasResponse extends HateoasResponse {
    
    private UUID id;
    
    private String username;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String fullName;
    
    private List<RoleResponse> roles;
    
    private Boolean isActive;
    
    private Boolean isEmailVerified;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime passwordChangedAt;
}
