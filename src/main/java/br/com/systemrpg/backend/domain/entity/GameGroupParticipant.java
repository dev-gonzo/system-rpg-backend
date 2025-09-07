package br.com.systemrpg.backend.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Entidade que representa a participação de um usuário em um grupo de jogo.
 */
@Entity
@Table(name = "game_group_participant", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"game_group_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"gameGroup", "user"})
@Getter
@Setter
public class GameGroupParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull(message = "{validation.gameGroupParticipant.gameGroup.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_group_id", nullable = false)
    private GameGroup gameGroup;

    @NotNull(message = "{validation.gameGroupParticipant.user.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "{validation.gameGroupParticipant.role.required}")
    @Min(value = 0, message = "{validation.gameGroupParticipant.role.min}")
    @Max(value = 2, message = "{validation.gameGroupParticipant.role.max}")
    @Column(name = "role", nullable = false)
    private Integer role; // 0 - MASTER, 1 - PLAYER, 2 - GUEST

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

    // Setters manuais para resolver problemas do Lombok
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getRole() {
        return role;
    }

    public User getUser() {
        return user;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setGameGroup(GameGroup gameGroup) {
        this.gameGroup = gameGroup;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setRole(Integer role) {
        this.role = role;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public GameGroup getGameGroup() {
        return gameGroup;
    }

    /**
     * Enum para roles de participante.
     */
    public enum ParticipantRole {
        MASTER(0),
        PLAYER(1),
        GUEST(2);

        private final int value;

        ParticipantRole(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ParticipantRole fromValue(int value) {
            for (ParticipantRole role : ParticipantRole.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid participant role value: " + value);
        }
    }
}