package br.com.systemrpg.backend.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import br.com.systemrpg.backend.constants.ValidationConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de usuários.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "{validation.username.required}")
    @Size(min = ValidationConstants.USERNAME_MIN_LENGTH, max = ValidationConstants.USERNAME_MAX_LENGTH, message = "{validation.username.size}")
    @Pattern(regexp = ValidationConstants.USERNAME_PATTERN, message = "{validation.username.pattern}")
    private String username;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Size(max = ValidationConstants.EMAIL_MAX_LENGTH, message = "{validation.email.size}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.PASSWORD_MAX_LENGTH, message = "{validation.password.size}")
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN,
            message = "{validation.password.pattern}")
    private String password;

    @NotBlank(message = "{validation.firstName.required}")
    @Size(min = ValidationConstants.FIRST_NAME_MIN_LENGTH, max = ValidationConstants.FIRST_NAME_MAX_LENGTH, message = "{validation.firstName.size}")
    private String firstName;

    @NotBlank(message = "{validation.lastName.required}")
    @Size(min = ValidationConstants.LAST_NAME_MIN_LENGTH, max = ValidationConstants.LAST_NAME_MAX_LENGTH, message = "{validation.lastName.size}")
    private String lastName;

    private Set<String> roles;

    // Getters manuais para resolver problemas do Lombok
    public Set<String> getRoles() {
        return roles;
    }

    public String getUsername() {
        return username;
    }
}
