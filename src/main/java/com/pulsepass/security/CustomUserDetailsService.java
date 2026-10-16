package com.pulsepass.security;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    UserCredentialRepository userCredentialRepository) {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
    }

    /**
     * Login: el identificador puede ser el username o el email (sin distinguir mayusculas).
     */
    @Override
    public SecurityPrincipal loadUserByUsername(String identifier) {
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return toPrincipal(user);
    }

    /**
     * Requests con JWT: el token solo trae el id; el estado y los roles se leen de nuevo
     * en cada request, asi un usuario desactivado pierde el acceso de inmediato (SEC-011).
     */
    public SecurityPrincipal loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return toPrincipal(user);
    }

    private SecurityPrincipal toPrincipal(User user) {
        // Un usuario creado antes de la Fase 4 no tiene credencial y no puede autenticarse
        UserCredential credential = userCredentialRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return SecurityPrincipal.from(user, credential);
    }
}
