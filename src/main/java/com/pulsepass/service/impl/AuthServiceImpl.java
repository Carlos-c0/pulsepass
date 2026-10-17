package com.pulsepass.service.impl;

import com.pulsepass.domain.Role;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.SecurityPrincipal;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String TOKEN_TYPE = "Bearer";

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserService userService,
                           UserRepository userRepository,
                           UserCredentialRepository userCredentialRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        // UserService conserva sus reglas (unicidad, fecha de nacimiento) y se une a esta transaccion
        UserResponse created = userService.register(request);

        // FR-SEC-AUTH-001: credencial BCrypt y rol USER en la misma transaccion que el usuario.
        // El rol nunca viene del request: el registro publico siempre crea USER (NFR-SEC-009)
        User user = userRepository.getReferenceById(created.id());
        userCredentialRepository.save(new UserCredential(
                user,
                passwordEncoder.encode(request.password()),
                LocalDateTime.now(),
                Set.of(Role.USER)
        ));

        return created;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.identifier(), request.password()));
        } catch (AuthenticationException ex) {
            // FR-SEC-AUTH-003: el mismo mensaje si el usuario no existe, la clave es incorrecta o esta inactivo
            log.warn("Login failed");
            throw new BadCredentialsException("Invalid credentials");
        }

        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
        log.info("Login succeeded for userId={}", principal.userId());

        return new AuthResponse(
                jwtService.generateToken(principal.userId()),
                TOKEN_TYPE,
                jwtService.getAccessTokenTtlSeconds()
        );
    }

    @Override
    public AuthenticatedUserResponse currentUser(SecurityPrincipal principal) {
        return new AuthenticatedUserResponse(
                principal.userId(),
                principal.getUsername(),
                principal.email(),
                principal.roles().stream().map(Role::name).sorted().toList()
        );
    }
}
