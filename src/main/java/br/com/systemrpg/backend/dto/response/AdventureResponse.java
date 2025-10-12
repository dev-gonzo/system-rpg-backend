package br.com.systemrpg.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdventureResponse {
    private UUID id;
    private String title;
    private String description;
    private Boolean isActive;
    private UUID gameGroupId;
    private LocalDateTime createdAt;
}