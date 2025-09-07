package br.com.systemrpg.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.exception.RecordNotFoundException;
import br.com.systemrpg.backend.repository.GameGroupRepository;
import br.com.systemrpg.backend.repository.GameGroupParticipantRepository;
import br.com.systemrpg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela lógica de negócio relacionada aos grupos de jogo.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GameGroupService {

    private static final Logger log = LoggerFactory.getLogger(GameGroupService.class);

    private final GameGroupRepository gameGroupRepository;
    private final GameGroupParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    /**
     * Lista todos os grupos de jogo com paginação.
     */
    @Transactional(readOnly = true)
    public Page<GameGroup> findAll(Pageable pageable) {
        return gameGroupRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }

    /**
     * Lista grupos de jogo ativos com paginação.
     */
    @Transactional(readOnly = true)
    public Page<GameGroup> findAllActive(Pageable pageable) {
        return gameGroupRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }

    /**
     * Busca grupos de jogo por filtros.
     */
    @Transactional(readOnly = true)
    public Page<GameGroup> findByFilters(String campaignName, String gameSystem, String settingWorld, Pageable pageable) {
        // Se todos os filtros estão vazios, retorna todos os grupos
        if (!StringUtils.hasText(campaignName) && !StringUtils.hasText(gameSystem) && !StringUtils.hasText(settingWorld)) {
            return findAll(pageable);
        }
        
        return gameGroupRepository.findByFiltersAndDeletedAtIsNull(
            StringUtils.hasText(campaignName) ? campaignName.trim() : null,
            StringUtils.hasText(gameSystem) ? gameSystem.trim() : null,
            StringUtils.hasText(settingWorld) ? settingWorld.trim() : null,
            pageable
        );
    }

    /**
     * Busca um grupo de jogo por ID.
     */
    @Transactional(readOnly = true)
    public GameGroup findById(UUID id) {
        return gameGroupRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.gameGroup.not.found", new Object[]{id}, LocaleContextHolder.getLocale())));
    }

    /**
     * Cria um novo grupo de jogo.
     */
    @Transactional
    public GameGroup createGameGroup(GameGroup gameGroup, UUID creatorUserId) {
        log.info("Criando novo grupo de jogo: {} por usuário: {}", gameGroup.getCampaignName(), creatorUserId);

        // Valida se o nome da campanha já existe
        if (gameGroupRepository.existsByCampaignNameIgnoreCaseAndDeletedAtIsNull(gameGroup.getCampaignName())) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroup.campaignName.exists", 
                    new Object[]{gameGroup.getCampaignName()}, LocaleContextHolder.getLocale()));
        }

        // Busca o usuário criador
        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.user.not.found", new Object[]{creatorUserId}, LocaleContextHolder.getLocale())));

        // Configura valores padrão
        gameGroup.setIsActive(true);
        gameGroup.setCreatedBy(creatorUserId);
        gameGroup.setCreatedAt(LocalDateTime.now());
        gameGroup.setUpdatedAt(LocalDateTime.now());

        // Salva o grupo
        GameGroup savedGroup = gameGroupRepository.save(gameGroup);

        // Adiciona o criador como MASTER
        GameGroupParticipant masterParticipant = new GameGroupParticipant();
        masterParticipant.setGameGroup(savedGroup);
        masterParticipant.setUser(creator);
        masterParticipant.setRole(GameGroupParticipant.ParticipantRole.MASTER.getValue());
        masterParticipant.setIsActive(true);
        masterParticipant.setCreatedAt(LocalDateTime.now());
        masterParticipant.setUpdatedAt(LocalDateTime.now());

        participantRepository.save(masterParticipant);

        log.info("Grupo de jogo criado com sucesso: {} (ID: {})", savedGroup.getCampaignName(), savedGroup.getId());
        return savedGroup;
    }

    /**
     * Atualiza um grupo de jogo existente.
     */
    @Transactional
    public GameGroup updateGameGroup(UUID id, GameGroup gameGroupUpdate) {
        log.info("Atualizando grupo de jogo: {}", id);

        GameGroup existingGroup = findById(id);

        // Valida se o nome da campanha já existe (excluindo o próprio grupo)
        if (StringUtils.hasText(gameGroupUpdate.getCampaignName()) && 
            !existingGroup.getCampaignName().equalsIgnoreCase(gameGroupUpdate.getCampaignName()) &&
            gameGroupRepository.existsByCampaignNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                gameGroupUpdate.getCampaignName(), id)) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroup.campaignName.exists", 
                    new Object[]{gameGroupUpdate.getCampaignName()}, LocaleContextHolder.getLocale()));
        }

        // Atualiza os campos
        if (StringUtils.hasText(gameGroupUpdate.getCampaignName())) {
            existingGroup.setCampaignName(gameGroupUpdate.getCampaignName());
        }
        if (gameGroupUpdate.getDescription() != null) {
            existingGroup.setDescription(gameGroupUpdate.getDescription());
        }
        if (StringUtils.hasText(gameGroupUpdate.getGameSystem())) {
            existingGroup.setGameSystem(gameGroupUpdate.getGameSystem());
        }
        if (gameGroupUpdate.getSettingWorld() != null) {
            existingGroup.setSettingWorld(gameGroupUpdate.getSettingWorld());
        }
        if (gameGroupUpdate.getShortDescription() != null) {
            existingGroup.setShortDescription(gameGroupUpdate.getShortDescription());
        }
        if (gameGroupUpdate.getMinPlayers() != null) {
            existingGroup.setMinPlayers(gameGroupUpdate.getMinPlayers());
        }
        if (gameGroupUpdate.getMaxPlayers() != null) {
            existingGroup.setMaxPlayers(gameGroupUpdate.getMaxPlayers());
        }
        if (gameGroupUpdate.getVisibility() != null) {
            existingGroup.setVisibility(gameGroupUpdate.getVisibility());
        }
        if (gameGroupUpdate.getAccessRule() != null) {
            existingGroup.setAccessRule(gameGroupUpdate.getAccessRule());
        }
        if (gameGroupUpdate.getModality() != null) {
            existingGroup.setModality(gameGroupUpdate.getModality());
        }
        if (gameGroupUpdate.getCountry() != null) {
            existingGroup.setCountry(gameGroupUpdate.getCountry());
        }
        if (gameGroupUpdate.getState() != null) {
            existingGroup.setState(gameGroupUpdate.getState());
        }
        if (gameGroupUpdate.getCity() != null) {
            existingGroup.setCity(gameGroupUpdate.getCity());
        }
        if (gameGroupUpdate.getThemesContent() != null) {
            existingGroup.setThemesContent(gameGroupUpdate.getThemesContent());
        }
        if (gameGroupUpdate.getPunctualityAttendance() != null) {
            existingGroup.setPunctualityAttendance(gameGroupUpdate.getPunctualityAttendance());
        }
        if (gameGroupUpdate.getHouseRules() != null) {
            existingGroup.setHouseRules(gameGroupUpdate.getHouseRules());
        }
        if (gameGroupUpdate.getBehavioralExpectations() != null) {
            existingGroup.setBehavioralExpectations(gameGroupUpdate.getBehavioralExpectations());
        }

        existingGroup.setUpdatedAt(LocalDateTime.now());

        GameGroup updatedGroup = gameGroupRepository.save(existingGroup);
        log.info("Grupo de jogo atualizado com sucesso: {}", updatedGroup.getId());
        return updatedGroup;
    }

    /**
     * Ativa ou desativa um grupo de jogo.
     */
    @Transactional
    public GameGroup toggleActiveStatus(UUID id) {
        log.info("Alterando status ativo do grupo de jogo: {}", id);

        GameGroup gameGroup = findById(id);
        gameGroup.setIsActive(!gameGroup.getIsActive());
        gameGroup.setUpdatedAt(LocalDateTime.now());

        GameGroup updatedGroup = gameGroupRepository.save(gameGroup);
        log.info("Status do grupo de jogo alterado para: {} (ID: {})", 
            updatedGroup.getIsActive() ? "ATIVO" : "INATIVO", updatedGroup.getId());
        return updatedGroup;
    }

    /**
     * Exclui logicamente um grupo de jogo.
     */
    @Transactional
    public void deleteGameGroup(UUID id) {
        log.info("Excluindo grupo de jogo: {}", id);

        GameGroup gameGroup = findById(id);
        gameGroup.setDeletedAt(LocalDateTime.now());
        gameGroup.setUpdatedAt(LocalDateTime.now());

        // Também exclui logicamente todos os participantes
        participantRepository.findByGameGroupIdAndDeletedAtIsNull(id)
            .forEach(participant -> {
                participant.setDeletedAt(LocalDateTime.now());
                participant.setUpdatedAt(LocalDateTime.now());
                participantRepository.save(participant);
            });

        gameGroupRepository.save(gameGroup);
        log.info("Grupo de jogo excluído com sucesso: {}", id);
    }

    /**
     * Verifica se um usuário é master de um grupo específico.
     */
    @Transactional(readOnly = true)
    public boolean isUserMasterOfGroup(UUID userId, UUID gameGroupId) {
        return participantRepository.findByGameGroupIdAndUserIdAndDeletedAtIsNull(gameGroupId, userId)
            .map(participant -> participant.getRole().equals(GameGroupParticipant.ParticipantRole.MASTER.getValue()))
            .orElse(false);
    }

    /**
     * Verifica se um usuário participa de um grupo específico.
     */
    @Transactional(readOnly = true)
    public boolean isUserParticipantOfGroup(UUID userId, UUID gameGroupId) {
        return participantRepository.existsByGameGroupIdAndUserIdAndDeletedAtIsNull(gameGroupId, userId);
    }

    /**
     * Verifica se um usuário pode visualizar um grupo (é participante ou master).
     */
    @Transactional(readOnly = true)
    public boolean canViewGroup(UUID gameGroupId, String username) {
        User user = userRepository.findByUsername(username)
            .orElse(null);
        
        if (user == null || user.getDeletedAt() != null) {
            return false;
        }
        
        return isUserParticipantOfGroup(user.getId(), gameGroupId);
    }

    /**
     * Verifica se um usuário é proprietário (master) de um grupo.
     */
    @Transactional(readOnly = true)
    public boolean isGroupOwner(UUID gameGroupId, String username) {
        User user = userRepository.findByUsername(username)
            .orElse(null);
        
        if (user == null || user.getDeletedAt() != null) {
            return false;
        }
        
        return isUserMasterOfGroup(user.getId(), gameGroupId);
    }

    /**
     * Lista todos os grupos que um usuário participa (como owner, player ou guest).
     */
    @Transactional(readOnly = true)
    public Page<GameGroup> findMyGameGroups(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.user.not.found", new Object[]{username}, LocaleContextHolder.getLocale())));
        
        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.user.inactive", new Object[]{username}, LocaleContextHolder.getLocale()));
        }
        
        // Busca todos os participantes do usuário
        List<GameGroupParticipant> participants = participantRepository.findByUserIdAndDeletedAtIsNull(user.getId());
        
        // Extrai os IDs dos grupos
        List<UUID> gameGroupIds = participants.stream()
            .map(participant -> participant.getGameGroup().getId())
            .distinct()
            .collect(Collectors.toList());
        
        if (gameGroupIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Busca os grupos com paginação
        return gameGroupRepository.findByIdsAndDeletedAtIsNull(gameGroupIds, pageable);
    }
}