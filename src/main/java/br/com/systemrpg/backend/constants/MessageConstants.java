package br.com.systemrpg.backend.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Classe que contém as constantes de mensagens utilizadas na aplicação.
 * Essas constantes são utilizadas para internacionalização e padronização das
 * mensagens de erro.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageConstants {

    public static final String ALREADY_EXISTS = "br.com.systemrpg.ALREADY_EXISTS";

    public static final String RECORD_NOT_FOUND = "br.com.systemrpg.RECORD_NOT_FOUND";

    public static final String UNAUTHORIZED = "br.com.systemrpg.UNAUTHORIZED";

    public static final String FORBIDDEN = "br.com.systemrpg.FORBIDDEN";

    public static final String INVALID_FIELDS = "br.com.systemrpg.INVALID_FIELDS";

    public static final String USER_ALREADY_EXISTS = "br.com.systemrpg.USER_ALREADY_EXISTS";

    public static final String INVALID_CREDENTIALS = "br.com.systemrpg.INVALID_CREDENTIALS";

    public static final String TOKEN_EXPIRED = "br.com.systemrpg.TOKEN_EXPIRED";

    public static final String INVALID_TOKEN = "br.com.systemrpg.INVALID_TOKEN";

    public static final String ACCESS_DENIED = "br.com.systemrpg.ACCESS_DENIED";
}
