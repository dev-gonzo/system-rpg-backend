package br.com.systemrpg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdventureUpdateRequest {

    @NotBlank(message = "dto.adventure.title.not.blank")
    @Size(min = 3, max = 100, message = "dto.adventure.title.size")
    private String title;

    @Size(max = 1000, message = "dto.adventure.description.size")
    private String description;
}