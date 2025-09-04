package br.com.systemrpg.backend.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de usuários.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "{validation.username.required}")
    @Size(min = 3, max = 50, message = "{validation.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "{validation.username.pattern}")
    private String username;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Size(max = 100, message = "{validation.email.size}")
    private String email;

    @Size(min = 8, max = 100, message = "{validation.password.size}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
             message = "{validation.password.pattern}")
    private String password;

    @NotBlank(message = "{validation.firstName.required}")
    @Size(min = 2, max = 50, message = "{validation.firstName.size}")
    private String firstName;

    @NotBlank(message = "{validation.lastName.required}")
    @Size(min = 2, max = 50, message = "{validation.lastName.size}")
    private String lastName;

    private Set<String> roles;

    // Getter manual para resolver problemas do Lombok
    public Set<String> getRoles() {
        return roles;
    }
}
