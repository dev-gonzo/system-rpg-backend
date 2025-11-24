package br.com.systemrpg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdventureNoteCreateRequest {

    @NotBlank(message = "{validation.adventurenote.title.required}")
    @Size(min = 3, max = 150, message = "{validation.adventurenote.title.size}")
    private String title;

    @NotBlank(message = "{validation.adventurenote.content.required}")
    private String content;
}