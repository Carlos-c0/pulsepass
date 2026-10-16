package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // Claves ficticias, exclusivas de las pruebas
    private static final String SECRETO = "cHVsc2VwYXNzLXRlc3Qtc2VjcmV0LXNvbG8tcGFyYS1wcnVlYmFzLTIwMjYhIQ==";
    private static final String OTRO_SECRETO = "cHVsc2VwYXNzLXRlc3Qtc2VjcmV0LWRlLTY0LWJ5dGVzLXNvbG8tcGFyYS1sYS1wcnVlYmEtZGUtaHM1MTIhIQ==";
    private static final String ISSUER = "pulsepass-api";
    private static final Duration QUINCE_MINUTOS = Duration.ofMinutes(15);

    private final JwtService jwtService = new JwtService(new JwtProperties(ISSUER, SECRETO, QUINCE_MINUTOS));

    @Test
    @DisplayName("FR-SEC-AUTH-005: el token lleva sub, iss, iat, exp y jti y dura 15 minutos")
    void tokenContieneLosClaimsObligatorios() {
        // ACT
        String token = jwtService.generateToken(7L);

        // ASSERT: se lee con la misma clave para revisar los claims
        Claims claims = Jwts.parser()
                .verifyWith(clave(SECRETO))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(QUINCE_MINUTOS.toMillis());
        assertThat(jwtService.getAccessTokenTtlSeconds()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Un token valido retorna el id del usuario")
    void tokenValidoRetornaElIdDelUsuario() {
        String token = jwtService.generateToken(7L);

        assertThat(jwtService.extractUserId(token)).contains(7L);
    }

    @Test
    @DisplayName("Cada token tiene un jti distinto")
    void cadaTokenTieneUnJtiDistinto() {
        String primero = jwtService.generateToken(7L);
        String segundo = jwtService.generateToken(7L);

        assertThat(primero).isNotEqualTo(segundo);
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token vencido se rechaza")
    void tokenVencidoSeRechaza() {
        // ARRANGE: el token se emitio hace una hora y duraba 15 minutos
        Clock haceUnaHora = Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC);
        JwtService emisorEnElPasado = new JwtService(new JwtProperties(ISSUER, SECRETO, QUINCE_MINUTOS), haceUnaHora);
        String token = emisorEnElPasado.generateToken(7L);

        // ACT + ASSERT
        assertThat(jwtService.extractUserId(token)).isEmpty();
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token con la firma alterada se rechaza")
    void tokenConFirmaAlteradaSeRechaza() {
        // ARRANGE: se cambia el primer caracter de la firma (el ultimo puede ser solo relleno)
        String token = jwtService.generateToken(7L);
        int inicioFirma = token.lastIndexOf('.') + 1;
        char original = token.charAt(inicioFirma);
        String alterado = token.substring(0, inicioFirma) + (original == 'A' ? 'B' : 'A')
                + token.substring(inicioFirma + 1);

        assertThat(jwtService.extractUserId(alterado)).isEmpty();
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token con el payload alterado se rechaza")
    void tokenConPayloadAlteradoSeRechaza() {
        // ARRANGE: se cambia el sub por el de otro usuario sin volver a firmar
        String token = jwtService.generateToken(7L);
        String[] partes = token.split("\\.");
        String otroPayload = jwtService.generateToken(8L).split("\\.")[1];
        String alterado = partes[0] + "." + otroPayload + "." + partes[2];

        assertThat(jwtService.extractUserId(alterado)).isEmpty();
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token de otro emisor se rechaza")
    void tokenDeOtroEmisorSeRechaza() {
        JwtService otroEmisor = new JwtService(new JwtProperties("otra-api", SECRETO, QUINCE_MINUTOS));
        String token = otroEmisor.generateToken(7L);

        assertThat(jwtService.extractUserId(token)).isEmpty();
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token firmado con otra clave se rechaza")
    void tokenFirmadoConOtraClaveSeRechaza() {
        JwtService otraClave = new JwtService(new JwtProperties(ISSUER, OTRO_SECRETO, QUINCE_MINUTOS));
        String token = otraClave.generateToken(7L);

        assertThat(jwtService.extractUserId(token)).isEmpty();
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token con un algoritmo distinto de HS256 se rechaza")
    void tokenConOtroAlgoritmoSeRechaza() {
        // ARRANGE: HS512 con una clave de 64 bytes, aceptada por JwtService para HS256
        JwtService servicioConClaveLarga = new JwtService(new JwtProperties(ISSUER, OTRO_SECRETO, QUINCE_MINUTOS));
        Date ahora = new Date();
        String token = Jwts.builder()
                .subject("7")
                .issuer(ISSUER)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + QUINCE_MINUTOS.toMillis()))
                .id("jti-de-prueba")
                .signWith(clave(OTRO_SECRETO), Jwts.SIG.HS512)
                .compact();

        assertThat(servicioConClaveLarga.extractUserId(token)).isEmpty();
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token sin firma (alg=none) se rechaza")
    void tokenSinFirmaSeRechaza() {
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes());
        String payload = jwtService.generateToken(7L).split("\\.")[1];

        assertThat(jwtService.extractUserId(header + "." + payload + ".")).isEmpty();
    }

    @Test
    @DisplayName("FR-SEC-AUTH-006: un token mal formado, vacio o nulo se rechaza")
    void tokenMalFormadoSeRechaza() {
        assertThat(jwtService.extractUserId("esto-no-es-un-jwt")).isEmpty();
        assertThat(jwtService.extractUserId("")).isEmpty();
        assertThat(jwtService.extractUserId(null)).isEmpty();
    }

    @Test
    @DisplayName("SEC-004: la aplicacion no arranca sin clave")
    void sinClaveNoArranca() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties(ISSUER, "", QUINCE_MINUTOS)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PULSEPASS_JWT_SECRET_BASE64");
    }

    @Test
    @DisplayName("SEC-004: la aplicacion no arranca con una clave de menos de 32 bytes")
    void claveCortaNoArranca() {
        String corta = java.util.Base64.getEncoder().encodeToString("clave-corta".getBytes());

        assertThatThrownBy(() -> new JwtService(new JwtProperties(ISSUER, corta, QUINCE_MINUTOS)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    private static SecretKey clave(String secretoBase64) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
    }
}
