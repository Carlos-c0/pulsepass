package com.pulsepass.service;

import com.pulsepass.domain.Role;

/**
 * Reglas de propiedad (anti-IDOR): compara el recurso pedido con la identidad autenticada.
 * Nunca confia en ids, emails o roles enviados por el cliente (SEC-006).
 */
public interface AuthorizationService {

    /**
     * true si el usuario autenticado tiene alguno de los roles; false para anonimos.
     */
    boolean hasAnyRole(Role... roles);

    /**
     * Permite continuar si el usuario autenticado es el propietario (mismo id persistente)
     * o tiene alguno de los roles indicados. Si no, lanza AccessDeniedException (403).
     * Un ownerId nulo nunca coincide: solo pasan los roles indicados.
     */
    void requireOwnerOrAnyRole(Long ownerId, Role... roles);
}
