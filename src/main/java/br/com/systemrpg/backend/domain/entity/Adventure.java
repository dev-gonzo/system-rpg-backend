package br.com.systemrpg.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "adventure")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Adventure {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank(message = "{validation.adventure.title.required}")
    @Size(min = 3, max = 100, message = "{validation.adventure.title.size}")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(max = 1000, message = "{validation.adventure.description.size}")
    @Column(name = "description", length = 1000)
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "{validation.adventure.createdBy.required}")
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_group_id", nullable = false)
    @NotNull(message = "{validation.adventure.gameGroupId.required}")
    private GameGroup gameGroup;

    // Getters específicos para compatibilidade
    public UUID getId() { return id; }
    public Boolean getIsActive() { return isActive; }
}