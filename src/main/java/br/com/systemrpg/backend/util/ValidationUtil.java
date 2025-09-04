package br.com.systemrpg.backend.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Utilitário para centralizar validações comuns.
 * Reduz a duplicação de código ao validar parâmetros.
 */
@Slf4j
public class ValidationUtil {

    /**
     * Construtor privado para prevenir instanciação.
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não deve ser instanciada");
    }

    /**
     * Valida se uma string não é nula nem vazia (após trim).
     *
     * @param value o valor a ser validado
     * @return true se a string é válida, false caso contrário
     */
    public static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Valida se uma string não é nula nem vazia (após trim).
     * Lança exceção se inválida.
     *
     * @param value o valor a ser validado
     * @param fieldName nome do campo para a mensagem de erro
     * @throws IllegalArgumentException se a string for inválida
     */
    public static void requireValidString(String value, String fieldName) {
        if (!isValidString(value)) {
            throw new IllegalArgumentException(fieldName + " não pode ser nulo ou vazio");
        }
    }

    /**
     * Valida se um objeto não é nulo.
     *
     * @param value o valor a ser validado
     * @return true se o objeto não é nulo, false caso contrário
     */
    public static boolean isNotNull(Object value) {
        return value != null;
    }

    /**
     * Valida se um objeto não é nulo.
     * Lança exceção se for nulo.
     *
     * @param value o valor a ser validado
     * @param fieldName nome do campo para a mensagem de erro
     * @throws IllegalArgumentException se o objeto for nulo
     */
    public static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " não pode ser nulo");
        }
    }

    /**
     * Valida se um header de autorização é válido (Bearer token).
     *
     * @param authHeader o header de autorização
     * @return true se o header é válido, false caso contrário
     */
    public static boolean isValidAuthHeader(String authHeader) {
        return isValidString(authHeader) && authHeader.startsWith("Bearer ") && authHeader.length() > 7;
    }

    /**
     * Valida se um email tem formato básico válido.
     *
     * @param email o email a ser validado
     * @return true se o email tem formato válido, false caso contrário
     */
    public static boolean isValidEmailFormat(String email) {
        if (!isValidString(email)) {
            return false;
        }
        return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
    }

    /**
     * Valida se um ID numérico é válido (maior que 0).
     *
     * @param id o ID a ser validado
     * @return true se o ID é válido, false caso contrário
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * Valida se uma string tem o comprimento mínimo exigido.
     *
     * @param value o valor a ser validado
     * @param minLength comprimento mínimo
     * @return true se atende ao comprimento mínimo, false caso contrário
     */
    public static boolean hasMinLength(String value, int minLength) {
        return isValidString(value) && value.trim().length() >= minLength;
    }

    /**
     * Valida se uma string não excede o comprimento máximo.
     *
     * @param value o valor a ser validado
     * @param maxLength comprimento máximo
     * @return true se não excede o comprimento máximo, false caso contrário
     */
    public static boolean hasMaxLength(String value, int maxLength) {
        return value == null || value.length() <= maxLength;
    }

    /**
     * Valida se uma string está dentro do intervalo de comprimento especificado.
     *
     * @param value o valor a ser validado
     * @param minLength comprimento mínimo
     * @param maxLength comprimento máximo
     * @return true se está dentro do intervalo, false caso contrário
     */
    public static boolean isLengthInRange(String value, int minLength, int maxLength) {
        return hasMinLength(value, minLength) && hasMaxLength(value, maxLength);
    }
}
