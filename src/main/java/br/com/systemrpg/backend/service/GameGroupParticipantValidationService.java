package br.com.systemrpg.backend.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.repository.GameGroupParticipantRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável por validações de participantes de grupos de jogo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameGroupParticipantValidationService {

    private static final Logger log = LoggerFactory.getLogger(GameGroupParticipantValidationService.class);
    private static final int MAX_GUESTS_PER_GROUP = 10;

    private final GameGroupParticipantRepository participantRepository;
    private final MessageSource messageSource;

    /**
     * Valida se é possível adicionar um novo participante ao grupo.
     * 
     * @param gameGroup O grupo de jogo
     * @param role O role do novo participante
     * @throws IllegalArgumentException se não for possível adicionar o participante
     */
    public void validateCanAddParticipant(GameGroup gameGroup, GameGroupParticipant.ParticipantRole role) {
        UUID gameGroupId = gameGroup.getId();
        
        log.debug("Validando adição de participante com role {} ao grupo {}", role, gameGroupId);
        
        // Busca todos os participantes ativos do grupo
        List<GameGroupParticipant> activeParticipants = participantRepository
            .findByGameGroupIdAndIsActiveTrueAndDeletedAtIsNull(gameGroupId);
        
        // Conta participantes por role
        long masterCount = activeParticipants.stream()
            .filter(p -> p.getRole().equals(GameGroupParticipant.ParticipantRole.MASTER.getValue()))
            .count();
        
        long playerCount = activeParticipants.stream()
            .filter(p -> p.getRole().equals(GameGroupParticipant.ParticipantRole.PLAYER.getValue()))
            .count();
        
        long guestCount = activeParticipants.stream()
            .filter(p -> p.getRole().equals(GameGroupParticipant.ParticipantRole.GUEST.getValue()))
            .count();
        
        log.debug("Participantes atuais - Master: {}, Player: {}, Guest: {}", masterCount, playerCount, guestCount);
        
        // Validações específicas por role
        switch (role) {
            case MASTER:
                if (masterCount >= 1) {
                    throw new IllegalArgumentException(messageSource
                        .getMessage("service.gameGroupParticipant.master.already.exists", 
                            new Object[]{gameGroupId}, LocaleContextHolder.getLocale()));
                }
                break;
                
            case PLAYER:
                // Valida limite de players baseado em maxPlayers do grupo
                if (gameGroup.getMaxPlayers() != null && playerCount >= gameGroup.getMaxPlayers()) {
                    throw new IllegalArgumentException(messageSource
                        .getMessage("service.gameGroupParticipant.max.players.reached", 
                            new Object[]{gameGroup.getMaxPlayers()}, LocaleContextHolder.getLocale()));
                }
                break;
                
            case GUEST:
                // Valida limite máximo de 10 guests por grupo
                if (guestCount >= MAX_GUESTS_PER_GROUP) {
                    throw new IllegalArgumentException(messageSource
                        .getMessage("service.gameGroupParticipant.max.guests.reached", 
                            new Object[]{MAX_GUESTS_PER_GROUP}, LocaleContextHolder.getLocale()));
                }
                break;
        }
        
        // Valida limite total de participantes (se definido)
        if (gameGroup.getMaxPlayers() != null) {
            long totalParticipants = participantRepository.countActiveParticipantsByGameGroupId(gameGroupId);
            if (totalParticipants >= gameGroup.getMaxPlayers()) {
                throw new IllegalArgumentException(messageSource
                    .getMessage("service.gameGroupParticipant.max.participants.reached", 
                        new Object[]{gameGroup.getMaxPlayers()}, LocaleContextHolder.getLocale()));
            }
        }
        
        log.debug("Validação concluída com sucesso para adição de participante com role {}", role);
    }
    
    /**
     * Valida se é possível usar um convite para entrar no grupo.
     * 
     * @param gameGroup O grupo de jogo
     * @param inviteRole O role do convite (PLAYER ou GUEST)
     * @throws IllegalArgumentException se não for possível usar o convite
     */
    public void validateCanUseInvite(GameGroup gameGroup, GameGroupParticipant.ParticipantRole inviteRole) {
        log.debug("Validando uso de convite com role {} para grupo {}", inviteRole, gameGroup.getId());
        
        // Reutiliza a validação de adição de participante
        validateCanAddParticipant(gameGroup, inviteRole);
        
        log.debug("Validação de uso de convite concluída com sucesso");
    }
    
    /**
     * Conta participantes ativos por role em um grupo.
     * 
     * @param gameGroupId ID do grupo
     * @return Array com [masterCount, playerCount, guestCount]
     */
    public long[] countParticipantsByRole(UUID gameGroupId) {
        List<GameGroupParticipant> activeParticipants = participantRepository
            .findByGameGroupIdAndIsActiveTrueAndDeletedAtIsNull(gameGroupId);
        
        long masterCount = activeParticipants.stream()
            .filter(p -> p.getRole().equals(GameGroupParticipant.ParticipantRole.MASTER.getValue()))
            .count();
        
        long playerCount = activeParticipants.stream()
            .filter(p -> p.getRole().equals(GameGroupParticipant.ParticipantRole.PLAYER.getValue()))
            .count();
        
        long guestCount = activeParticipants.stream()
            .filter(p -> p.getRole().equals(GameGroupParticipant.ParticipantRole.GUEST.getValue()))
            .count();
        
        return new long[]{masterCount, playerCount, guestCount};
    }
    
    /**
     * Verifica se um grupo tem vagas disponíveis para um determinado role.
     * 
     * @param gameGroup O grupo de jogo
     * @param role O role desejado
     * @return true se há vagas disponíveis
     */
    public boolean hasAvailableSlots(GameGroup gameGroup, GameGroupParticipant.ParticipantRole role) {
        try {
            validateCanAddParticipant(gameGroup, role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Retorna o número máximo de guests permitidos por grupo.
     * 
     * @return Número máximo de guests
     */
    public int getMaxGuestsPerGroup() {
        return MAX_GUESTS_PER_GROUP;
    }
}