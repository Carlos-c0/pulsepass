package com.pulsepass.support;

import com.pulsepass.domain.Role;
import com.pulsepass.security.SecurityPrincipal;

import java.util.Set;

/**
 * Identidades de prueba del escenario del PRD de seguridad (seccion 23).
 * Se usan con .with(user(...)) de spring-security-test; los hashes son ficticios.
 */
public final class TestPrincipals {

    private static final String HASH_FICTICIO = "$2a$10$hashFicticioSoloParaPruebas";

    private TestPrincipals() {
    }

    public static SecurityPrincipal andrea() {
        return new SecurityPrincipal(1L, "andrea", "andrea@email.com", HASH_FICTICIO, true, Set.of(Role.USER));
    }

    public static SecurityPrincipal carlos() {
        return new SecurityPrincipal(2L, "carlos", "carlos@email.com", HASH_FICTICIO, true, Set.of(Role.USER));
    }

    public static SecurityPrincipal laura() {
        return new SecurityPrincipal(3L, "laura", "laura@email.com", HASH_FICTICIO, true, Set.of(Role.STAFF));
    }

    public static SecurityPrincipal admin() {
        return new SecurityPrincipal(4L, "admin", "admin@pulsepass.com", HASH_FICTICIO, true, Set.of(Role.ADMIN));
    }
}
