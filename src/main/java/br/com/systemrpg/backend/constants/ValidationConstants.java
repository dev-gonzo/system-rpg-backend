package br.com.systemrpg.backend.constants;

/**
 * Constantes para validação de dados.
 * Centraliza valores de validação para evitar magic numbers.
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Classe utilitária - construtor privado
    }

    // Validações de Username
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]+$";

    // Validações de Email
    public static final int EMAIL_MAX_LENGTH = 100;

    // Validações de Password
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;
    
    /**
     * REGEX PATTERN para validação de senha - NÃO É UMA SENHA HARDCODED.
     * Este é um padrão de expressão regular usado para validar formato de senhas.
     * Exige: pelo menos 1 minúscula, 1 maiúscula, 1 dígito e 1 caractere especial.
     * 
     * SONARQUBE: Esta constante contém apenas uma REGEX de validação,
     * não contém credenciais reais. É seguro manter esta configuração.
     * 
     * @see <a href="https://regex101.com">Teste esta regex</a>
     */
    @SuppressWarnings("java:S2068") // Suprime warning de senha hardcoded - esta é uma regex de validação
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";

    // Validações de Nome
    public static final int FIRST_NAME_MIN_LENGTH = 2;
    public static final int FIRST_NAME_MAX_LENGTH = 50;
    public static final int LAST_NAME_MIN_LENGTH = 2;
    public static final int LAST_NAME_MAX_LENGTH = 50;
    public static final int FULL_NAME_MAX_LENGTH = 100;

    // Validações de Role
    public static final int ROLE_NAME_MIN_LENGTH = 2;
    public static final int ROLE_NAME_MAX_LENGTH = 50;
    public static final int ROLE_DESCRIPTION_MAX_LENGTH = 255;

    // Validações de Token
    public static final int TOKEN_HASH_LENGTH = 64;
    public static final int TOKEN_REASON_MAX_LENGTH = 100;
    public static final int TOKEN_MASK_PREFIX_LENGTH = 10;
    public static final int TOKEN_MASK_SUFFIX_LENGTH = 4;
    public static final int TOKEN_MIN_LENGTH_FOR_MASK = 10;

    // Validações de Paginação
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Validações de RSA
    public static final int RSA_KEY_SIZE = 2048;

    // Validações de Base64
    public static final String BASE64_PADDING = "=";
}
