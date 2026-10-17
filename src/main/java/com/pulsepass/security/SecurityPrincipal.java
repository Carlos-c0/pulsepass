package com.pulsepass.security;

import com.pulsepass.domain.Role;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * Identidad autenticada que Spring Security guarda en el contexto de cada request.
 * Se construye siempre desde la base de datos, nunca desde datos enviados por el cliente.
 */
public record SecurityPrincipal(
        Long userId,
        String username,
        String email,
        String passwordHash,
        boolean active,
        Set<Role> roles
) implements UserDetails {

    public SecurityPrincipal {
        roles = Set.copyOf(roles);
    }

    public static SecurityPrincipal from(User user, UserCredential credential) {
        return new SecurityPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                credential.getPasswordHash(),
                Boolean.TRUE.equals(user.getActive()),
                credential.getRoles()
        );
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    // Un record imprime todos sus campos; se excluye el hash para que nunca llegue a un log (SEC-003)
    @Override
    public String toString() {
        return "SecurityPrincipal[userId=" + userId + ", username=" + username + ", roles=" + roles + "]";
    }
}
