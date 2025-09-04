package br.com.systemrpg.backend.exception;

/**
 * Exceção lançada quando há falha na geração de JWK (JSON Web Key).
 */
public class JwkGenerationException extends RuntimeException {
    
    public JwkGenerationException(String message) {
        super(message);
    }
    
    public JwkGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
