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

    @NotBlank(message = "{validation.gameGroup.shortDescription.required}")
    @Size(min = 3, max = 100, message = "{validation.gameGroup.shortDescription.size}")
    @Column(name = "shortDescription", nullable = false, length = 100)
    private String shortDescription;

    @NotNull(message = "{validation.gameGroup.visibility.required}")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility;

    @NotNull(message = "{validation.gameGroup.accessRule.required}")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "access_rule", nullable = false)
    private AccessRule accessRule;

    @NotNull(message = "{validation.gameGroup.modality.required}")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "modality", nullable = false)
    private Modality modality;

    @Min(value = 1, message = "{validation.gameGroup.minPlayers.min}")
    @Max(value = 20, message = "{validation.gameGroup.minPlayers.max}")
    @Column(name = "min_players")
    private Integer minPlayers;

    @Min(value = 1, message = "{validation.gameGroup.maxPlayers.min}")
    @Max(value = 20, message = "{validation.gameGroup.maxPlayers.max}")
    @Column(name = "max_players")
    private Integer maxPlayers;

    @Size(max = 100, message = "{validation.gameGroup.country.size}")
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 100, message = "{validation.gameGroup.state.size}")
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 100, message = "{validation.gameGroup.city.size}")
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 500, message = "{validation.gameGroup.themesContent.size}")
    @Column(name = "themes_content", length = 500)
    private String themesContent;

    @Size(max = 500, message = "{validation.gameGroup.punctualityAttendance.size}")
    @Column(name = "punctuality_attendance", length = 500)
    private String punctualityAttendance;

    @Size(max = 500, message = "{validation.gameGroup.houseRules.size}")
    @Column(name = "house_rules", length = 500)
    private String houseRules;

    @Size(max = 500, message = "{validation.gameGroup.behavioralExpectations.size}")
    @Column(name = "behavioral_expectations", length = 500)
    private String behavioralExpectations;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "{validation.gameGroup.createdBy.required}")
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

    public AccessRule getAccessRule() {
        return accessRule;
    }

    public Modality getModality() {
        return modality;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setAccessRule(AccessRule accessRule) {
        this.accessRule = accessRule;
    }

    public void setModality(Modality modality) {
        this.modality = modality;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
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
     * Enum para modalidade do grupo.
     */
    public enum Modality {
        ONLINE(0),
        PRESENTIAL(1);

        private final int value;

        Modality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Modality fromValue(int value) {
            for (Modality modality : Modality.values()) {
                if (modality.value == value) {
                    return modality;
                }
            }
            throw new IllegalArgumentException("Invalid modality value: " + value);
        }
    }

    /**
     * Enum para visibilidade do grupo.
     */
    public enum Visibility {
        PUBLIC(0),
        FRIENDS(1),
        PRIVATE(2);

        private final int value;

        Visibility(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Visibility fromValue(int value) {
            for (Visibility visibility : Visibility.values()) {
                if (visibility.value == value) {
                    return visibility;
                }
            }
            throw new IllegalArgumentException("Invalid visibility value: " + value);
        }
    }
}