package com.pulsepass.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Lee el encabezado Authorization: Bearer, valida el JWT y deja la identidad en el contexto.
 * No es un @Component a proposito: si lo fuera, Spring Boot tambien lo registraria como
 * filtro normal del servidor y correria fuera de la cadena de seguridad. Lo crea SecurityConfig.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String INVALID_TOKEN = "Invalid or expired token";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        List<String> headers = Collections.list(request.getHeaders(HttpHeaders.AUTHORIZATION));

        // Sin encabezado: el request sigue como anonimo y la cadena decide si la ruta es publica
        if (headers.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Dos encabezados Authorization o un esquema distinto de Bearer: se rechaza
        if (headers.size() > 1 || !headers.get(0).regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            reject(request, response, "Invalid authorization header");
            return;
        }

        String token = headers.get(0).substring(BEARER_PREFIX.length()).trim();
        Optional<Long> userId = jwtService.extractUserId(token);

        // Un Bearer invalido nunca se trata como anonimo: siempre responde 401
        if (userId.isEmpty()) {
            reject(request, response, INVALID_TOKEN);
            return;
        }

        SecurityPrincipal principal;
        try {
            principal = userDetailsService.loadUserById(userId.get());
        } catch (UsernameNotFoundException ex) {
            reject(request, response, INVALID_TOKEN);
            return;
        }

        // El estado se revisa en cada request: un usuario desactivado pierde el acceso (SEC-011)
        if (!principal.isEnabled()) {
            reject(request, response, INVALID_TOKEN);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, new BadCredentialsException(message));
    }
}
