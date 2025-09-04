package br.com.systemrpg.backend.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import br.com.systemrpg.backend.repository.TokenBlacklistRepository;

@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private TokenCleanupService tokenCleanupService;



    @Test
    void dailyCleanup_WithExpiredTokens_ShouldDeleteExpiredTokens() {
        // Arrange
        when(tokenBlacklistRepository.deleteExpiredTokens(any(LocalDateTime.class)))
            .thenReturn(2);
        when(tokenBlacklistRepository.count()).thenReturn(5L);

        // Act
        tokenCleanupService.dailyCleanup();

        // Assert
        verify(tokenBlacklistRepository).deleteExpiredTokens(any(LocalDateTime.class));
        verify(tokenBlacklistRepository).count();
    }

    @Test
    void dailyCleanup_WithNoExpiredTokens_ShouldNotDeleteAnyTokens() {
        // Arrange
        when(tokenBlacklistRepository.deleteExpiredTokens(any(LocalDateTime.class)))
            .thenReturn(0);
        when(tokenBlacklistRepository.count()).thenReturn(3L);

        // Act
        tokenCleanupService.dailyCleanup();

        // Assert
        verify(tokenBlacklistRepository).deleteExpiredTokens(any(LocalDateTime.class));
        verify(tokenBlacklistRepository).count();
    }



    @Test
    void dailyCleanup_WhenRepositoryThrowsException_ShouldHandleGracefully() {
        // Arrange
        when(tokenBlacklistRepository.deleteExpiredTokens(any(LocalDateTime.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        // O método deve lidar com a exceção graciosamente
        // Não deve propagar a exceção para cima
        try {
            tokenCleanupService.dailyCleanup();
        } catch (Exception e) {
            // Se uma exceção for lançada, o teste falhará
            throw new AssertionError("O método deveria lidar com exceções graciosamente", e);
        }

        verify(tokenBlacklistRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void dailyCleanup_WhenCountThrowsException_ShouldHandleGracefully() {
        // Arrange
        when(tokenBlacklistRepository.deleteExpiredTokens(any(LocalDateTime.class)))
            .thenReturn(1);
        when(tokenBlacklistRepository.count())
            .thenThrow(new RuntimeException("Count error"));

        // Act & Assert
        try {
            tokenCleanupService.dailyCleanup();
        } catch (Exception e) {
            throw new AssertionError("O método deveria lidar com exceções graciosamente", e);
        }

        verify(tokenBlacklistRepository).deleteExpiredTokens(any(LocalDateTime.class));
        verify(tokenBlacklistRepository).count();
    }

    @Test
    void dailyCleanup_ShouldUseCurrentTimeForComparison() {
        // Arrange
        when(tokenBlacklistRepository.deleteExpiredTokens(any(LocalDateTime.class)))
            .thenReturn(0);
        when(tokenBlacklistRepository.count()).thenReturn(0L);

        // Act
        tokenCleanupService.dailyCleanup();

        // Assert
        // Verifica que o método foi chamado com um timestamp próximo ao atual
        verify(tokenBlacklistRepository).deleteExpiredTokens(argThat(dateTime -> {
            LocalDateTime now = LocalDateTime.now();
            // Permite uma diferença de até 1 segundo
            return dateTime.isAfter(now.minusSeconds(1)) && dateTime.isBefore(now.plusSeconds(1));
        }));
    }
}
