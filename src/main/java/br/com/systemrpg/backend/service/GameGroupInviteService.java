package br.com.systemrpg.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
import br.com.systemrpg.backend.domain.entity.GameGroupInvite;
import br.com.systemrpg.backend.domain.entity.GameGroupParticipant;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.exception.RecordNotFoundException;
import br.com.systemrpg.backend.repository.GameGroupInviteRepository;
import br.com.systemrpg.backend.repository.GameGroupParticipantRepository;
import br.com.systemrpg.backend.repository.GameGroupRepository;
import br.com.systemrpg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela lógica de negócio relacionada aos convites de grupos de jogo.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GameGroupInviteService {

    private static final Logger log = LoggerFactory.getLogger(GameGroupInviteService.class);
    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final int MAX_INVITE_GENERATION_ATTEMPTS = 10;

    private final GameGroupInviteRepository inviteRepository;
    private final GameGroupRepository gameGroupRepository;
    private final GameGroupParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final GameGroupParticipantValidationService validationService;
    private final MessageSource messageSource;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Cria um novo convite para um grupo de jogo.
     * 
     * @param gameGroupId ID do grupo
     * @param creatorUsername Username do criador do convite
     * @param role Role do convite (PLAYER ou GUEST)
     * @param isUniqueUse Se o convite é de uso único
     * @param expiresAt Data de expiração (opcional)
     * @return O convite criado
     */
    @Transactional
    public GameGroupInvite createInvite(UUID gameGroupId, String creatorUsername, 
                                       GameGroupInvite.InviteRole role, Boolean isUniqueUse, 
                                       LocalDateTime expiresAt) {
        log.info("Criando convite para grupo {} por usuário {} com role {}", gameGroupId, creatorUsername, role);

        // Busca o grupo
        GameGroup gameGroup = gameGroupRepository.findByIdAndDeletedAtIsNull(gameGroupId)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.gameGroup.not.found", new Object[]{gameGroupId}, LocaleContextHolder.getLocale())));

        if (!gameGroup.getIsActive()) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroup.inactive", new Object[]{gameGroupId}, LocaleContextHolder.getLocale()));
        }

        // Busca o usuário criador
        User creator = userRepository.findByUsername(creatorUsername)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.user.not.found", new Object[]{creatorUsername}, LocaleContextHolder.getLocale())));

        if (!creator.getIsActive()) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.user.inactive", new Object[]{creatorUsername}, LocaleContextHolder.getLocale()));
        }

        // Verifica se o usuário é master do grupo
        if (!isUserMasterOfGroup(creator.getId(), gameGroupId)) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroupInvite.only.master.can.create", 
                    new Object[]{gameGroupId}, LocaleContextHolder.getLocale()));
        }

        // Valida se é possível adicionar participantes com o role especificado
        GameGroupParticipant.ParticipantRole participantRole = convertInviteRoleToParticipantRole(role);
        validationService.validateCanAddParticipant(gameGroup, participantRole);

        // Gera código único do convite
        String inviteCode = generateUniqueInviteCode();

        // Cria o convite
        GameGroupInvite invite = new GameGroupInvite();
        invite.setGameGroup(gameGroup);
        invite.setInviteCode(inviteCode);
        invite.setRole(role.getValue());
        invite.setIsUniqueUse(isUniqueUse != null ? isUniqueUse : false);
        invite.setIsUsed(false);
        invite.setExpiresAt(expiresAt);
        invite.setCreatedByUser(creator);

        GameGroupInvite savedInvite = inviteRepository.save(invite);
        log.info("Convite criado com sucesso: {} para grupo {}", savedInvite.getId(), gameGroupId);
        
        return savedInvite;
    }

    /**
     * Usa um convite para entrar em um grupo.
     * 
     * @param inviteCode Código do convite
     * @param username Username do usuário que quer usar o convite
     * @return O participante criado
     */
    @Transactional
    public GameGroupParticipant useInvite(String inviteCode, String username) {
        log.info("Usuário {} tentando usar convite {}", username, inviteCode);

        // Busca o convite
        GameGroupInvite invite = inviteRepository.findByInviteCodeAndDeletedAtIsNull(inviteCode)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.gameGroupInvite.not.found", new Object[]{inviteCode}, LocaleContextHolder.getLocale())));

        // Verifica se o convite é válido
        if (!invite.isValid()) {
            if (invite.getIsUsed()) {
                throw new IllegalArgumentException(messageSource
                    .getMessage("service.gameGroupInvite.already.used", new Object[]{inviteCode}, LocaleContextHolder.getLocale()));
            }
            if (invite.isExpired()) {
                throw new IllegalArgumentException(messageSource
                    .getMessage("service.gameGroupInvite.expired", new Object[]{inviteCode}, LocaleContextHolder.getLocale()));
            }
        }

        // Busca o usuário
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.user.not.found", new Object[]{username}, LocaleContextHolder.getLocale())));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.user.inactive", new Object[]{username}, LocaleContextHolder.getLocale()));
        }

        GameGroup gameGroup = invite.getGameGroup();
        
        // Verifica se o usuário já participa do grupo
        if (participantRepository.existsByGameGroupIdAndUserIdAndDeletedAtIsNull(gameGroup.getId(), user.getId())) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroupParticipant.already.exists", 
                    new Object[]{user.getId(), gameGroup.getId()}, LocaleContextHolder.getLocale()));
        }

        // Valida se é possível usar o convite
        GameGroupParticipant.ParticipantRole participantRole = convertInviteRoleToParticipantRole(
            GameGroupInvite.InviteRole.fromValue(invite.getRole()));
        validationService.validateCanUseInvite(gameGroup, participantRole);

        // Cria o participante
        GameGroupParticipant participant = new GameGroupParticipant();
        participant.setGameGroup(gameGroup);
        participant.setUser(user);
        participant.setRole(participantRole.getValue());
        participant.setIsActive(true);

        GameGroupParticipant savedParticipant = participantRepository.save(participant);

        // Marca o convite como usado (se for de uso único)
        if (invite.getIsUniqueUse()) {
            invite.markAsUsed(user);
            inviteRepository.save(invite);
        }

        log.info("Convite {} usado com sucesso por usuário {}, participante criado: {}", 
            inviteCode, username, savedParticipant.getId());
        
        return savedParticipant;
    }

    /**
     * Lista convites de um grupo.
     */
    @Transactional(readOnly = true)
    public List<GameGroupInvite> findByGameGroupId(UUID gameGroupId) {
        return inviteRepository.findByGameGroupIdAndDeletedAtIsNull(gameGroupId);
    }

    /**
     * Lista convites de um grupo com paginação.
     */
    @Transactional(readOnly = true)
    public Page<GameGroupInvite> findByGameGroupId(UUID gameGroupId, Pageable pageable) {
        return inviteRepository.findByGameGroupIdAndDeletedAtIsNull(gameGroupId, pageable);
    }

    /**
     * Lista convites válidos de um grupo.
     */
    @Transactional(readOnly = true)
    public List<GameGroupInvite> findValidInvitesByGameGroupId(UUID gameGroupId) {
        return inviteRepository.findValidInvitesByGameGroupId(gameGroupId, LocalDateTime.now());
    }

    /**
     * Busca um convite por código.
     */
    @Transactional(readOnly = true)
    public Optional<GameGroupInvite> findByInviteCode(String inviteCode) {
        return inviteRepository.findByInviteCodeAndDeletedAtIsNull(inviteCode);
    }

    /**
     * Remove um convite (soft delete).
     */
    @Transactional
    public void deleteInvite(UUID inviteId, String username) {
        log.info("Removendo convite {} por usuário {}", inviteId, username);

        GameGroupInvite invite = inviteRepository.findByIdAndDeletedAtIsNull(inviteId)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.gameGroupInvite.not.found", new Object[]{inviteId}, LocaleContextHolder.getLocale())));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RecordNotFoundException(messageSource
                .getMessage("service.user.not.found", new Object[]{username}, LocaleContextHolder.getLocale())));

        // Verifica se o usuário pode remover o convite (deve ser master do grupo ou criador do convite)
        if (!isUserMasterOfGroup(user.getId(), invite.getGameGroup().getId()) && 
            !invite.getCreatedByUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(messageSource
                .getMessage("service.gameGroupInvite.cannot.delete", 
                    new Object[]{inviteId}, LocaleContextHolder.getLocale()));
        }

        invite.setDeletedAt(LocalDateTime.now());
        inviteRepository.save(invite);
        
        log.info("Convite {} removido com sucesso", inviteId);
    }

    /**
     * Remove convites expirados.
     */
    @Transactional
    public int cleanupExpiredInvites() {
        log.info("Removendo convites expirados");
        int deletedCount = inviteRepository.deleteExpiredInvites(LocalDateTime.now());
        log.info("Removidos {} convites expirados", deletedCount);
        return deletedCount;
    }

    /**
     * Gera um código único para o convite.
     */
    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < MAX_INVITE_GENERATION_ATTEMPTS; attempt++) {
            String code = generateRandomCode();
            if (!inviteRepository.existsValidInviteByCode(code, LocalDateTime.now())) {
                return code;
            }
        }
        throw new RuntimeException("Não foi possível gerar um código único para o convite após " + 
            MAX_INVITE_GENERATION_ATTEMPTS + " tentativas");
    }

    /**
     * Gera um código aleatório.
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            code.append(INVITE_CODE_CHARS.charAt(secureRandom.nextInt(INVITE_CODE_CHARS.length())));
        }
        return code.toString();
    }

    /**
     * Verifica se um usuário é master de um grupo.
     */
    private boolean isUserMasterOfGroup(UUID userId, UUID gameGroupId) {
        return participantRepository.findMasterByGameGroupIdAndDeletedAtIsNull(gameGroupId)
            .map(master -> master.getUser().getId().equals(userId))
            .orElse(false);
    }

    /**
     * Verifica se um usuário pode deletar um convite.
     * Usado para validação de segurança nos endpoints.
     */
    @Transactional(readOnly = true)
    public boolean canDeleteInvite(UUID inviteId, String username) {
        try {
            Optional<GameGroupInvite> inviteOpt = inviteRepository.findByIdAndDeletedAtIsNull(inviteId);
            if (inviteOpt.isEmpty()) {
                return false;
            }
            
            GameGroupInvite invite = inviteOpt.get();
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // Usuário pode deletar se for master do grupo ou criador do convite
            return isUserMasterOfGroup(user.getId(), invite.getGameGroup().getId()) || 
                   invite.getCreatedByUser().getId().equals(user.getId());
                   
        } catch (Exception e) {
            log.error("Erro ao verificar permissão para deletar convite {}: {}", inviteId, e.getMessage());
            return false;
        }
    }

    /**
     * Converte InviteRole para ParticipantRole.
     */
    private GameGroupParticipant.ParticipantRole convertInviteRoleToParticipantRole(GameGroupInvite.InviteRole inviteRole) {
        switch (inviteRole) {
            case PLAYER:
                return GameGroupParticipant.ParticipantRole.PLAYER;
            case GUEST:
                return GameGroupParticipant.ParticipantRole.GUEST;
            default:
                throw new IllegalArgumentException("Invalid invite role: " + inviteRole);
        }
    }
}