package br.com.systemrpg.backend.util;

public class TokenUtils {

    private TokenUtils() {
        // Utility class - private constructor to prevent instantiation
    }

    private static final String BEARER_PREFIX = "Bearer ";

    public static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
}

