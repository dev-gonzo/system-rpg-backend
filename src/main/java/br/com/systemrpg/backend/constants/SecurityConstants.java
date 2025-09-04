package br.com.systemrpg.backend.constants;

/**
 * Constantes relacionadas à segurança e autenticação.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Classe utilitária - não deve ser instanciada
    }

    // Timeouts e durações
    public static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 3600L; // 1 hora
    public static final long CORS_MAX_AGE_SECONDS = 3600L; // 1 hora
    public static final long HSTS_MAX_AGE_SECONDS = 31536000L; // 1 ano
    public static final long MESSAGE_CACHE_SECONDS = 3600L; // 1 hora
    public static final long PERFORMANCE_THRESHOLD_MS = 1000L; // 1 segundo
    
    // Conversões de tempo
    public static final long MILLISECONDS_TO_SECONDS_DIVISOR = 1000L;
    
    // Códigos de status HTTP
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    
    // Limites de tamanho
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_REASON_LENGTH = 100;
}
