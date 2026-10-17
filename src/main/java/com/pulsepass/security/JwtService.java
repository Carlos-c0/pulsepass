package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtService {

    static final String EXPECTED_ALGORITHM = "HS256";
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTokenTtl;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    // Constructor para pruebas: permite fijar el reloj y fabricar tokens vencidos
    JwtService(JwtProperties properties, Clock clock) {
        this.key = buildKey(properties.secretBase64());
        this.issuer = requireIssuer(properties.issuer());
        this.accessTokenTtl = requireTtl(properties.accessTokenTtl());
        this.clock = clock;
    }

    public String generateToken(Long userId) {
        Instant now = clock.instant();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }

    /**
     * Retorna el id del usuario si el token es valido: firma correcta, algoritmo HS256,
     * emisor esperado, no vencido y con sub, iat, exp y jti presentes.
     * Cualquier otro caso retorna vacio; nunca lanza la excepcion al llamador.
     */
    public Optional<Long> extractUserId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token);

            if (!EXPECTED_ALGORITHM.equals(jws.getHeader().getAlgorithm())) {
                return Optional.empty();
            }

            Claims claims = jws.getPayload();
            if (claims.getSubject() == null || claims.getIssuedAt() == null
                    || claims.getExpiration() == null || claims.getId() == null) {
                return Optional.empty();
            }

            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException ex) {
            // Incluye token vencido, firma alterada, emisor distinto, formato invalido y sub no numerico
            return Optional.empty();
        }
    }

    private static SecretKey buildKey(String secretBase64) {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 is not configured");
        }

        byte[] secret;
        try {
            secret = Decoders.BASE64.decode(secretBase64);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 is not valid Base64");
        }

        if (secret.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "PULSEPASS_JWT_SECRET_BASE64 must decode to at least " + MIN_SECRET_BYTES + " bytes");
        }

        return Keys.hmacShaKeyFor(secret);
    }

    private static String requireIssuer(String issuer) {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("JWT issuer is not configured");
        }
        return issuer;
    }

    private static Duration requireTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalStateException("JWT access token TTL must be positive");
        }
        return ttl;
    }
}
