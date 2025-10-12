package br.com.systemrpg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AdventureCreateRequest {
    @NotNull(message = "{validation.adventure.gameGroupId.required}")
    private UUID gameGroupId;

    @NotBlank(message = "{validation.adventure.title.required}")
    @Size(min = 3, max = 100, message = "{validation.adventure.title.size}")
    private String title;

    @Size(max = 1000, message = "{validation.adventure.description.size}")
    private String description;

    // Getter manual para resolver possíveis problemas do Lombok em runtime (SpEL)
    public java.util.UUID getGameGroupId() {
        return gameGroupId;
    }
}