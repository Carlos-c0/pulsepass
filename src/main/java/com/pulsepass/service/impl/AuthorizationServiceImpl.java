package com.pulsepass.service.impl;

import com.pulsepass.domain.Role;
import com.pulsepass.security.SecurityPrincipal;
import com.pulsepass.service.AuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public boolean hasAnyRole(Role... roles) {
        return currentPrincipal()
                .map(principal -> hasAny(principal, roles))
                .orElse(false);
    }

    @Override
    public void requireOwnerOrAnyRole(Long ownerId, Role... roles) {
        SecurityPrincipal principal = currentPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        boolean isOwner = ownerId != null && Objects.equals(principal.userId(), ownerId);

        if (!isOwner && !hasAny(principal, roles)) {
            throw new AccessDeniedException("User " + principal.userId() + " cannot access a resource of another user");
        }
    }

    private Optional<SecurityPrincipal> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof SecurityPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    private boolean hasAny(SecurityPrincipal principal, Role... roles) {
        return Arrays.stream(roles).anyMatch(principal::hasRole);
    }
}
