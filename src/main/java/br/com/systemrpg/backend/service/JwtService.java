package br.com.systemrpg.backend.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import br.com.systemrpg.backend.constants.ValidationConstants;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import br.com.systemrpg.backend.dto.JwkKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.exception.JwkGenerationException;
import br.com.systemrpg.backend.exception.KeyGenerationException;
import br.com.systemrpg.backend.repository.TokenBlacklistRepository;
import br.com.systemrpg.backend.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para geração e validação de tokens JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final MessageSource messageSource;

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String secretKey;

    @Value("${jwt.access-token.expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;
    
    @Value("${jwt.rsa.algorithm:RS256}")
    private String algorithm;
    
    @Value("${jwt.rsa.key-id:systemrpg-backend-key-2025}")
    private String keyId;
    
    @Value("${jwt.rsa.private-key:}")
    private String rsaPrivateKeyBase64;
    
    @Value("${jwt.rsa.public-key:}")
    private String rsaPublicKeyBase64;
    
    // Constantes para claims do JWT
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_FULL_NAME = "fullName";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String RSA_ALGORITHM = "RS256";
    
    // Cache para o par de chaves RSA (carregado das configurações)
    private KeyPair rsaKeyPair;

    /**
     * Extrai o username do token JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai uma claim específica do token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Gera um access token para o usuário.
     */
    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(CLAIM_USER_ID, user.getId().toString());
        extraClaims.put(CLAIM_EMAIL, user.getEmail());
        extraClaims.put(CLAIM_FULL_NAME, user.getFullName());
        extraClaims.put(CLAIM_ROLES, user.getRoles().stream()
                .map(role -> role.getName())
                .toList());
        extraClaims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        
        return generateToken(extraClaims, user.getUsername(), accessTokenExpiration);
    }

    /**
     * Gera um refresh token para o usuário.
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(CLAIM_USER_ID, user.getId().toString());
        extraClaims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        
        return generateToken(extraClaims, user.getUsername(), refreshTokenExpiration);
    }

    /**
     * Gera um token JWT com claims extras.
     */
    public String generateToken(Map<String, Object> extraClaims, String username, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        if (RSA_ALGORITHM.equals(algorithm)) {
            return generateRsaToken(extraClaims, username, now, expiryDate);
        } else {
            return generateHmacToken(extraClaims, username, now, expiryDate);
        }
    }

    /**
     * Gera token usando algoritmo RSA.
     */
    private String generateRsaToken(Map<String, Object> extraClaims, String username, Date now, Date expiryDate) {
        KeyPair keyPair = getRsaKeyPair();
        return Jwts.builder()
                .header()
                    .keyId(keyId)
                    .and()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Gera token usando algoritmo HMAC.
     */
    private String generateHmacToken(Map<String, Object> extraClaims, String username, Date now, Date expiryDate) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Valida se o token é válido para o usuário.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && 
                    !isTokenExpired(token) && 
                    !isTokenBlacklisted(token));
        } catch (Exception e) {
            log.warn(messageSource
                    .getMessage("service.jwt.token.invalid", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
            return false;
        }
    }

    /**
     * Verifica se o token está expirado.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Verifica se o token está na blacklist.
     */
    public boolean isTokenBlacklisted(String token) {
        String tokenHash = generateTokenHash(token);
        return tokenBlacklistRepository.existsByTokenHash(tokenHash);
    }

    /**
     * Extrai a data de expiração do token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai o tipo do token (ACCESS ou REFRESH).
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    /**
     * Extrai o ID do usuário do token.
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_USER_ID, String.class));
    }

    /**
     * Gera um hash do token para armazenamento na blacklist.
     */
    public String generateTokenHash(String token) {
        return Integer.toHexString(token.hashCode());
    }

    /**
     * Extrai todas as claims do token.
     */
    private Claims extractAllClaims(String token) {
        if (RSA_ALGORITHM.equals(algorithm)) {
            return extractClaimsWithRsa(token);
        } else {
            return extractClaimsWithHmac(token);
        }
    }

    /**
     * Extrai claims usando RSA com fallback para HMAC.
     */
    private Claims extractClaimsWithRsa(String token) {
        try {
            KeyPair keyPair = getRsaKeyPair();
            return Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("Falha ao validar token com RSA, tentando HMAC: {}", e.getMessage());
            return extractClaimsWithHmac(token);
        }
    }

    /**
     * Extrai claims usando HMAC.
     */
    private Claims extractClaimsWithHmac(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtém a chave de assinatura.
     */
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converte Date para LocalDateTime.
     */
    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    /**
     * Carrega ou gera o par de chaves RSA.
     */
    private synchronized KeyPair getRsaKeyPair() {
        if (rsaKeyPair == null) {
            try {
                // Se as chaves estão configuradas e não são vazias, carrega elas
                if (rsaPrivateKeyBase64 != null && !rsaPrivateKeyBase64.trim().isEmpty() && 
                    rsaPublicKeyBase64 != null && !rsaPublicKeyBase64.trim().isEmpty()) {
                    rsaKeyPair = loadRsaKeyPairFromConfig();
                    log.info("Par de chaves RSA carregado das configurações para JWKS");
                } else {
                    // Fallback: gera novas chaves (não recomendado para produção)
                    rsaKeyPair = generateNewRsaKeyPair();
                    log.warn("Par de chaves RSA gerado dinamicamente - configure chaves fixas para produção");
                }
            } catch (Exception e) {
                log.error("Erro ao carregar/gerar par de chaves RSA: {}", e.getMessage());
                // Em caso de erro, tenta gerar novas chaves como fallback
                try {
                    rsaKeyPair = generateNewRsaKeyPair();
                    log.warn("Usando chaves RSA geradas dinamicamente devido ao erro anterior");
                } catch (NoSuchAlgorithmException fallbackException) {
                    throw new KeyGenerationException("Falha ao obter chaves RSA", fallbackException);
                }
            }
        }
        return rsaKeyPair;
    }
    
    /**
     * Carrega o par de chaves RSA das configurações.
     */
    private KeyPair loadRsaKeyPairFromConfig() throws Exception {
        // Decodifica a chave privada
        byte[] privateKeyBytes = Base64.getDecoder().decode(rsaPrivateKeyBase64);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        
        // Decodifica a chave pública
        byte[] publicKeyBytes = Base64.getDecoder().decode(rsaPublicKeyBase64);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        
        return new KeyPair(publicKey, privateKey);
    }
    
    /**
     * Gera um novo par de chaves RSA (fallback).
     */
    private KeyPair generateNewRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(ValidationConstants.RSA_KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }
    
    /**
     * Gera uma JWK (JSON Web Key) para a chave pública atual.
     */
    public JwkKey generateJwkKey() {
        if ("HS256".equals(algorithm)) {
            // Para HMAC, não podemos expor a chave secreta
            // Retornamos uma JWK vazia ou lançamos exceção
            log.warn("JWKS não suportado para algoritmo HMAC ({}). Configure RSA para usar JWKS.", algorithm);
            return null;
        }
        
        try {
            KeyPair keyPair = getRsaKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            
            // Extrai o módulo (n) e expoente (e) da chave pública RSA
            String modulus = base64UrlEncode(publicKey.getModulus().toByteArray());
            String exponent = base64UrlEncode(publicKey.getPublicExponent().toByteArray());
            
            return JwkKey.createRsaSigningKey(keyId, RSA_ALGORITHM, modulus, exponent);
            
        } catch (Exception e) {
            log.error("Erro ao gerar JWK: {}", e.getMessage());
            throw new JwkGenerationException("Falha ao gerar JWK", e);
        }
    }
    
    /**
     * Codifica bytes em Base64URL (sem padding).
     */
    private String base64UrlEncode(byte[] bytes) {
        // Remove bytes de sinal se presentes (para números positivos)
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Realiza a introspecção do token, validando e extraindo suas claims.
     * 
     * @param token O token JWT a ser validado
     * @return Map com as claims do token se válido, null se inválido
     */
    public Map<String, Object> introspectToken(String token) {
        try {
            if (!isTokenValidForIntrospection(token)) {
                return new HashMap<>();
            }

            Claims claims = extractAllClaims(token);
            return buildClaimsMap(claims);
            
        } catch (Exception e) {
            log.warn("Erro durante introspecção do token: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Valida se o token é válido para introspecção.
     */
    private boolean isTokenValidForIntrospection(String token) {
        if (isTokenBlacklisted(token)) {
            log.warn("Token na blacklist durante introspecção: {}", maskToken(token));
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);
            if (claims.getExpiration().before(new Date())) {
                log.warn("Token expirado durante introspecção: {}", maskToken(token));
                return false;
            }
        } catch (Exception e) {
            log.warn("Erro ao validar token para introspecção: {}", e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Constrói o mapa de claims a partir das claims do JWT.
     */
    private Map<String, Object> buildClaimsMap(Claims claims) {
        Map<String, Object> claimsMap = new HashMap<>();
        
        // Claims padrão
        claimsMap.put("sub", claims.getSubject());
        claimsMap.put("exp", claims.getExpiration().getTime() / SecurityConstants.MILLISECONDS_TO_SECONDS_DIVISOR); // Unix timestamp
        claimsMap.put("iat", claims.getIssuedAt().getTime() / SecurityConstants.MILLISECONDS_TO_SECONDS_DIVISOR); // Unix timestamp
        
        // Claims customizadas
        addCustomClaimIfPresent(claimsMap, claims, CLAIM_USER_ID, CLAIM_USER_ID);
        addCustomClaimIfPresent(claimsMap, claims, CLAIM_EMAIL, CLAIM_EMAIL);
        addCustomClaimIfPresent(claimsMap, claims, CLAIM_FULL_NAME, "name");
        addCustomClaimIfPresent(claimsMap, claims, CLAIM_ROLES, CLAIM_ROLES);
        addCustomClaimIfPresent(claimsMap, claims, CLAIM_TOKEN_TYPE, CLAIM_TOKEN_TYPE);

        return claimsMap;
    }

    /**
     * Adiciona uma claim customizada ao mapa se ela estiver presente.
     */
    private void addCustomClaimIfPresent(Map<String, Object> claimsMap, Claims claims, String claimKey, String mapKey) {
        Object claimValue = claims.get(claimKey);
        if (claimValue != null) {
            claimsMap.put(mapKey, claimValue);
        }
    }

    /**
     * Mascara o token para logs de segurança.
     */
    private String maskToken(String token) {
        if (token == null || token.length() < ValidationConstants.TOKEN_MIN_LENGTH_FOR_MASK) {
            return "***";
        }
        return token.substring(0, ValidationConstants.TOKEN_MASK_PREFIX_LENGTH) + "..." + 
               token.substring(token.length() - ValidationConstants.TOKEN_MASK_SUFFIX_LENGTH);
    }
}
