package br.com.systemrpg.backend.domain.entity;

import java.time.LocalDateTime;
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
 * Entidade que representa um convite para participar de um grupo de jogo.
 */
@Entity
@Table(name = "game_group_invite", 
       indexes = {
           @Index(name = "idx_invite_code", columnList = "invite_code", unique = true),
           @Index(name = "idx_game_group_id", columnList = "game_group_id"),
           @Index(name = "idx_created_by", columnList = "created_by")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"gameGroup", "createdByUser", "usedByUser"})
@Getter
@Setter
public class GameGroupInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull(message = "{validation.gameGroupInvite.gameGroup.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_group_id", nullable = false)
    private GameGroup gameGroup;

    @NotBlank(message = "{validation.gameGroupInvite.inviteCode.required}")
    @Size(min = 8, max = 32, message = "{validation.gameGroupInvite.inviteCode.size}")
    @Column(name = "invite_code", nullable = false, unique = true, length = 32)
    private String inviteCode;

    @NotNull(message = "{validation.gameGroupInvite.role.required}")
    @Min(value = 1, message = "{validation.gameGroupInvite.role.min}")
    @Max(value = 2, message = "{validation.gameGroupInvite.role.max}")
    @Column(name = "role", nullable = false)
    private Integer role; // 1 - PLAYER, 2 - GUEST (não permite MASTER)

    @Builder.Default
    @Column(name = "is_unique_use", nullable = false)
    private Boolean isUniqueUse = false;

    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by")
    private User usedByUser;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @NotNull(message = "{validation.gameGroupInvite.createdBy.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdByUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Getters manuais para resolver problemas do Lombok
    public Boolean getIsUsed() {
        return isUsed;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    public Integer getRole() {
        return role;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }
    
    public UUID getId() {
        return id;
    }
    
    public Boolean getIsUniqueUse() {
        return isUniqueUse;
    }
    
    public void setGameGroup(GameGroup gameGroup) {
        this.gameGroup = gameGroup;
    }
    
    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
    
    public void setRole(Integer role) {
        this.role = role;
    }
    
    public void setIsUniqueUse(Boolean isUniqueUse) {
        this.isUniqueUse = isUniqueUse;
    }
    
    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    /**
     * Enum para roles de convite.
     */
    public enum InviteRole {
        PLAYER(1),
        GUEST(2);

        private final int value;

        InviteRole(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static InviteRole fromValue(int value) {
            for (InviteRole role : InviteRole.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid invite role value: " + value);
        }
    }

    /**
     * Verifica se o convite está válido (não usado, não expirado, não deletado).
     */
    public boolean isValid() {
        return !isUsed && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now())) &&
               deletedAt == null;
    }

    /**
     * Verifica se o convite expirou.
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Marca o convite como usado.
     */
    public void markAsUsed(User user) {
        this.isUsed = true;
        this.usedByUser = user;
        this.usedAt = LocalDateTime.now();
    }
}