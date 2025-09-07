package br.com.systemrpg.backend.service;

import java.time.LocalDateTime;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.systemrpg.backend.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serviço responsável pela limpeza automática de tokens expirados da blacklist.
 */
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final MessageSource messageSource;

    /**
     * Executa a limpeza de tokens expirados diariamente à meia-noite
     *  Remove tokens da blacklist que já expiraram para otimizar o banco de dados.
     *  expeiracoa via banco, cuidado com multiplas instancia
     *  Importante! Deve ser avalidado se essa rotina pode ser feita direramente no banco
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void dailyCleanup() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            log.info(messageSource
                    .getMessage("service.token.cleanup.started", new Object[]{now}, LocaleContextHolder.getLocale()));
            
            // Remove tokens expirados
            int deletedCount = tokenBlacklistRepository.deleteExpiredTokens(now);
            
            // Conta total de tokens na blacklist após limpeza
            long totalTokens = tokenBlacklistRepository.count();
            
            log.info(messageSource
                    .getMessage("service.token.cleanup.completed", new Object[]{deletedCount, totalTokens}, LocaleContextHolder.getLocale()));
            
        } catch (Exception e) {
            log.error(messageSource
                    .getMessage("service.token.cleanup.error", null, LocaleContextHolder.getLocale()), e);
        }
    }

}
