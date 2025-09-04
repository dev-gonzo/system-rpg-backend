package br.com.systemrpg.backend.exception;

/**
 * Exceção lançada quando há falha na geração de chaves RSA.
 */
public class KeyGenerationException extends RuntimeException {
    
    public KeyGenerationException(String message) {
        super(message);
    }
    
    public KeyGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
