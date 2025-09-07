package br.com.systemrpg.backend.domain.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Entidade que representa um grupo de jogo no sistema.
 */
@Entity
@Table(name = "game_group")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"participants"})
@Getter
@Setter
public class GameGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank(message = "{validation.gameGroup.campaignName.required}")
    @Size(min = 3, max = 100, message = "{validation.gameGroup.campaignName.size}")
    @Column(name = "campaign_name", nullable = false, length = 100)
    private String campaignName;

    @Size(max = 500, message = "{validation.gameGroup.description.size}")
    @Column(name = "description", length = 500)
    private String description;

    @NotBlank(message = "{validation.gameGroup.gameSystem.required}")
    @Size(min = 2, max = 100, message = "{validation.gameGroup.gameSystem.size}")
    @Column(name = "game_system", nullable = false, length = 100)
    private String gameSystem;

    @Size(max = 100, message = "{validation.gameGroup.settingWorld.size}")
    @Column(name = "setting_world", length = 100)
    private String settingWorld;

    @NotNull(message = "{validation.gameGroup.accessRule.required}")
    @Min(value = 0, message = "{validation.gameGroup.accessRule.min}")
    @Max(value = 2, message = "{validation.gameGroup.accessRule.max}")
    @Column(name = "access_rule", nullable = false)
    private Integer accessRule; // 0 - free, 1 - friends, 2 - approval

    @NotNull(message = "{validation.gameGroup.modality.required}")
    @Min(value = 0, message = "{validation.gameGroup.modality.min}")
    @Max(value = 1, message = "{validation.gameGroup.modality.max}")
    @Column(name = "modality", nullable = false)
    private Integer modality; // 0 - online, 1 - presencial

    @Min(value = 1, message = "{validation.gameGroup.minPlayers.min}")
    @Max(value = 20, message = "{validation.gameGroup.minPlayers.max}")
    @Column(name = "min_players")
    private Integer minPlayers;

    @Min(value = 1, message = "{validation.gameGroup.maxPlayers.min}")
    @Max(value = 20, message = "{validation.gameGroup.maxPlayers.max}")
    @Column(name = "max_players")
    private Integer maxPlayers;

    @Min(value = 1, message = "{validation.gameGroup.maxParticipants.min}")
    @Max(value = 20, message = "{validation.gameGroup.maxParticipants.max}")
    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "gameGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<GameGroupParticipant> participants = new HashSet<>();

    // Getters manuais para resolver problemas do Lombok
    public UUID getId() {
        return id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGameSystem() {
        return gameSystem;
    }

    public String getSettingWorld() {
        return settingWorld;
    }

    public void setSettingWorld(String settingWorld) {
        this.settingWorld = settingWorld;
    }

    public Integer getAccessRule() {
        return accessRule;
    }

    public Integer getModality() {
        return modality;
    }

    public void setAccessRule(Integer accessRule) {
        this.accessRule = accessRule;
    }

    public void setModality(Integer modality) {
        this.modality = modality;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * Enum para regras de acesso.
     */
    public enum AccessRule {
        FREE(0),
        FRIENDS(1),
        APPROVAL(2);

        private final int value;

        AccessRule(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AccessRule fromValue(int value) {
            for (AccessRule rule : AccessRule.values()) {
                if (rule.getValue() == value) {
                    return rule;
                }
            }
            throw new IllegalArgumentException("Invalid access rule value: " + value);
        }
    }

    /**
     * Enum para modalidade.
     */
    public enum Modality {
        ONLINE(0),
        PRESENCIAL(1);

        private final int value;

        Modality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Modality fromValue(int value) {
            for (Modality modality : Modality.values()) {
                if (modality.getValue() == value) {
                    return modality;
                }
            }
            throw new IllegalArgumentException("Invalid modality value: " + value);
        }
    }
}