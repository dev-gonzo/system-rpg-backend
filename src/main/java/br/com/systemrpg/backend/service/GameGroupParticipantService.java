package br.com.systemrpg.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.systemrpg.backend.domain.entity.GameGroup;
import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.dto.request.GameGroupParticipantCreateRequest;
import br.com.systemrpg.backend.dto.response.GameGroupParticipantResponse;
import br.com.systemrpg.backend.exception.RecordNotFoundException;
import br.com.systemrpg.backend.mapper.GameGroupParticipantMapper;
import br.com.systemrpg.backend.repository.GameGroupRepository;
import br.com.systemrpg.backend.repository.GameGroupParticipantRepository;
import br.com.systemrpg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela lógica de negócio relacionada aos participantes de grupos de jogo.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GameGroupParticipantService {

    private static final Logger log = LoggerFactory.getLogger(GameGroupParticipantService.class);

    private final GameGroupParticipantRepository participantRepository;
    private final GameGroupRepository gameGroupRepository;
    private final UserRepository userRepository;
    private final GameGroupParticipantMapper gameGroupParticipantMapper;
    private final MessageSource messageSource;

    /**
     * Lista todos os participantes com paginação.
     */
    @Transactional(readOnly = true)
    public Page<GameGroupParticipant> findAll(Pageable pageable) {
        return participantRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }

    /**
     * Lista todos os participantes com paginação e filtros, retornando como Response DTO.
     */
    @Transactional(readOnly = true)
    public Page<GameGroupParticipantResponse> findAll(Pageable pageable, UUID gameGroupId, UUID userId, String role, Boolean isActive) {
        // Por simplicidade, vamos usar apenas paginação básica por enquanto
        // TODO: Implementar filtros específicos no repository
        Page<GameGroupParticipant> participants = participantRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
        return participants.map(gameGroupParticipantMapper::toResponse);
    }

    /**
     * Lista todos os participantes de um grupo específico.
     */
    @Transactional(readOnly = true)
    public List<GameGroupParticipant> findByGameGroupId(UUID gameGroupId) {
        return participantRepository.findByGameGroupIdAndDeletedAtIsNull(gameGroupId);
    }

    /**
     * Lista participantes ativos de um grupo específico.
     */
    @Transactional(readOnly = true)
    public List<GameGroupParticipant> findActiveByGameGroupId(UUID gameGroupId) {
        return participantRepository.findByGameGroupIdAndIsActiveTrueAndDeletedAtIsNull(gameGroupId);
    }

    /**
     * Lista todos os grupos que um usuário participa.
     */
    @Transactional(readOnly = true)
    public List<GameGroupParticipant> findByUserId(UUID userId) {
        return participantRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    /**
     * Busca um participante por ID.
     */
    @Transactional(readOnly = true)
    public GameGroupParticipant findById(UUID id) {
        return participantRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.gameGroupParticipant.not.found", new Object[]{id}, LocaleContextHolder.getLocale())));
    }

    /**
     * Busca um participante por ID e retorna como Response DTO.
     */
    @Transactional(readOnly = true)
    public GameGroupParticipantResponse findByIdAsResponse(UUID id) {
        GameGroupParticipant participant = findById(id);
        return gameGroupParticipantMapper.toResponse(participant);
    }

    /**
     * Adiciona um participante a um grupo de jogo.
     */
    @Transactional
    public GameGroupParticipant addParticipant(UUID gameGroupId, UUID userId, GameGroupParticipant.ParticipantRole role) {
        log.info("Adicionando participante {} ao grupo {} com role {}", userId, gameGroupId, role);

        // Verifica se o grupo existe e está ativo
        GameGroup gameGroup = gameGroupRepository.findByIdAndDeletedAtIsNull(gameGroupId)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.gameGroup.not.found", new Object[]{gameGroupId}, LocaleContextHolder.getLocale())));

        if (!gameGroup.getIsActive()) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroup.inactive", new Object[]{gameGroupId}, LocaleContextHolder.getLocale()));
        }

        // Verifica se o usuário existe e está ativo
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.user.not.found", new Object[]{userId}, LocaleContextHolder.getLocale())));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.user.inactive", new Object[]{userId}, LocaleContextHolder.getLocale()));
        }

        // Verifica se o usuário já participa do grupo
        if (participantRepository.existsByGameGroupIdAndUserIdAndDeletedAtIsNull(gameGroupId, userId)) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroupParticipant.already.exists", 
                    new Object[]{userId, gameGroupId}, LocaleContextHolder.getLocale()));
        }

        // Verifica se já existe um master no grupo (apenas um master por grupo)
        if (role == GameGroupParticipant.ParticipantRole.MASTER) {
            if (participantRepository.findMasterByGameGroupIdAndDeletedAtIsNull(gameGroupId).isPresent()) {
                throw new IllegalArgumentException(messageSource
                    .getMessage("service.gameGroupParticipant.master.already.exists", 
                        new Object[]{gameGroupId}, LocaleContextHolder.getLocale()));
            }
        }

        // Verifica limite de participantes
        if (gameGroup.getMaxParticipants() != null) {
            long currentParticipants = participantRepository.countActiveParticipantsByGameGroupId(gameGroupId);
            if (currentParticipants >= gameGroup.getMaxParticipants()) {
                throw new IllegalArgumentException(messageSource
                    .getMessage("service.gameGroupParticipant.max.participants.reached", 
                        new Object[]{gameGroup.getMaxParticipants()}, LocaleContextHolder.getLocale()));
            }
        }

        // Cria o participante
        GameGroupParticipant participant = new GameGroupParticipant();
        participant.setGameGroup(gameGroup);
        participant.setUser(user);
        participant.setRole(role.getValue());
        participant.setIsActive(true);
        participant.setCreatedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());

        GameGroupParticipant savedParticipant = participantRepository.save(participant);
        log.info("Participante adicionado com sucesso: {} ao grupo {}", userId, gameGroupId);
        return savedParticipant;
    }

    /**
     * Cria um participante a partir de um request DTO.
     */
    @Transactional
    public GameGroupParticipantResponse create(GameGroupParticipantCreateRequest request) {
        GameGroupParticipant.ParticipantRole role = GameGroupParticipant.ParticipantRole.PLAYER; // padrão
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                role = GameGroupParticipant.ParticipantRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Role inválido: " + request.getRole());
            }
        }
        
        GameGroupParticipant participant = addParticipant(request.getGameGroupId(), request.getUserId(), role);
        return gameGroupParticipantMapper.toResponse(participant);
    }

    /**
     * Ativa ou desativa um participante.
     */
    @Transactional
    public GameGroupParticipant toggleActiveStatus(UUID id) {
        log.info("Alterando status ativo do participante: {}", id);

        GameGroupParticipant participant = findById(id);
        
        // Não permite desativar o master
        if (participant.getRole().equals(GameGroupParticipant.ParticipantRole.MASTER.getValue()) && participant.getIsActive()) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroupParticipant.master.cannot.deactivate", 
                    new Object[]{id}, LocaleContextHolder.getLocale()));
        }

        participant.setIsActive(!participant.getIsActive());
        participant.setUpdatedAt(LocalDateTime.now());

        GameGroupParticipant updatedParticipant = participantRepository.save(participant);
        log.info("Status do participante alterado para: {} (ID: {})", 
            updatedParticipant.getIsActive() ? "ATIVO" : "INATIVO", updatedParticipant.getId());
        return updatedParticipant;
    }

    /**
     * Remove um participante de um grupo de jogo (exclusão lógica).
     */
    @Transactional
    public void removeParticipant(UUID id) {
        log.info("Removendo participante: {}", id);

        GameGroupParticipant participant = findById(id);
        
        // Não permite remover o master
        if (participant.getRole().equals(GameGroupParticipant.ParticipantRole.MASTER.getValue())) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroupParticipant.master.cannot.remove", 
                    new Object[]{id}, LocaleContextHolder.getLocale()));
        }

        participant.setDeletedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());

        participantRepository.save(participant);
        log.info("Participante removido com sucesso: {}", id);
    }

    /**
     * Busca o master de um grupo específico.
     */
    @Transactional(readOnly = true)
    public GameGroupParticipant findMasterByGameGroupId(UUID gameGroupId) {
        return participantRepository.findMasterByGameGroupIdAndDeletedAtIsNull(gameGroupId)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.gameGroupParticipant.master.not.found", 
                    new Object[]{gameGroupId}, LocaleContextHolder.getLocale())));
    }

    /**
     * Conta o número de participantes ativos em um grupo.
     */
    @Transactional(readOnly = true)
    public long countActiveParticipants(UUID gameGroupId) {
        return participantRepository.countActiveParticipantsByGameGroupId(gameGroupId);
    }

    /**
     * Verifica se um usuário é proprietário de um participante específico (é o próprio participante).
     */
    @Transactional(readOnly = true)
    public boolean isParticipantOwner(UUID participantId, String username) {
        User user = userRepository.findByUsername(username)
            .orElse(null);
        
        if (user == null || user.getDeletedAt() != null) {
            return false;
        }
        
        GameGroupParticipant participant = participantRepository.findByIdAndDeletedAtIsNull(participantId)
            .orElse(null);
        
        if (participant == null) {
            return false;
        }
        
        // O usuário pode ver seus próprios dados de participação ou se for master do grupo
        return participant.getUser().getId().equals(user.getId()) || 
               isUserMasterOfGroup(user.getId(), participant.getGameGroup().getId());
    }

    /**
     * Verifica se um usuário pode gerenciar um participante (é master do grupo).
     */
    @Transactional(readOnly = true)
    public boolean canManageParticipant(UUID participantId, String username) {
        User user = userRepository.findByUsername(username)
            .orElse(null);
        
        if (user == null || user.getDeletedAt() != null) {
            return false;
        }
        
        GameGroupParticipant participant = participantRepository.findByIdAndDeletedAtIsNull(participantId)
            .orElse(null);
        
        if (participant == null) {
            return false;
        }
        
        // Apenas o master do grupo pode gerenciar participantes
        return isUserMasterOfGroup(user.getId(), participant.getGameGroup().getId());
    }

    /**
     * Verifica se um usuário é master de um grupo específico.
     */
    @Transactional(readOnly = true)
    private boolean isUserMasterOfGroup(UUID userId, UUID gameGroupId) {
        return participantRepository.findByGameGroupIdAndUserIdAndDeletedAtIsNull(gameGroupId, userId)
            .map(participant -> participant.getRole().equals(GameGroupParticipant.ParticipantRole.MASTER.getValue()))
            .orElse(false);
    }
}