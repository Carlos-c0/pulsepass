package com.pulsepass;

import com.jayway.jsonpath.JsonPath;
import com.pulsepass.domain.Role;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo con la base de datos real (Testcontainers): registro, login, /me y propiedad.
 * Cada prueba corre en una transaccion que se revierte al final.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class SecurityIntegrationTest {

    // Contrasena ficticia, exclusiva de las pruebas
    private static final String CLAVE = "UnaFraseSeguraLarga2026!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("T-SEC-001: el registro guarda la contrasena con BCrypt y el rol USER")
    void registroGuardaHashYRolUser() throws Exception {
        // ACT + ASSERT: la respuesta nunca incluye la contrasena
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro("andrea")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist());

        User andrea = userRepository.findByUsername("andrea").orElseThrow();
        UserCredential credencial = userCredentialRepository.findById(andrea.getId()).orElseThrow();

        assertThat(credencial.getPasswordHash()).startsWith("$2").isNotEqualTo(CLAVE);
        assertThat(passwordEncoder.matches(CLAVE, credencial.getPasswordHash())).isTrue();
        assertThat(credencial.getRoles()).containsExactly(Role.USER);
    }

    @Test
    @DisplayName("T-SEC-004: login retorna un Bearer y /me identifica al usuario")
    void loginYMeConTokenReal() throws Exception {
        // ARRANGE
        registrar("andrea");

        // ACT
        String token = login("andrea", CLAVE);

        // ASSERT
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    @DisplayName("FR-SEC-AUTH-002: tambien se inicia sesion con el email")
    void loginConEmail() throws Exception {
        registrar("andrea");

        assertThat(login("andrea@email.com", CLAVE)).isNotBlank();
    }

    @Test
    @DisplayName("T-SEC-005: una contrasena incorrecta responde 401 con mensaje generico")
    void loginConClaveIncorrectaRetorna401() throws Exception {
        registrar("andrea");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("andrea", "ClaveIncorrecta2026!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("Un usuario anterior a la Fase 4, sin credencial, no puede iniciar sesion")
    void usuarioSinCredencialNoIniciaSesion() throws Exception {
        // ARRANGE: usuario creado sin contrasena (como los de las fases anteriores)
        userRepository.save(new User("legado", "legado@email.com"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("legado", CLAVE)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("T-SEC-015: con su propio token, un USER no puede leer los datos de otro (403)")
    void usuarioNoLeeDatosDeOtro() throws Exception {
        // ARRANGE
        registrar("andrea");
        registrar("carlos");
        String tokenCarlos = login("carlos", CLAVE);

        // ACT + ASSERT
        mockMvc.perform(get("/api/users/by-email")
                        .header("Authorization", "Bearer " + tokenCarlos)
                        .param("email", "andrea@email.com"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.email").doesNotExist());

        // Sus propios datos si los puede leer
        mockMvc.perform(get("/api/users/by-email")
                        .header("Authorization", "Bearer " + tokenCarlos)
                        .param("email", "carlos@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("carlos"));
    }

    @Test
    @DisplayName("T-SEC-008: un token con la firma alterada responde 401")
    void tokenAlteradoRetorna401() throws Exception {
        // ARRANGE: se cambia el primer caracter de la firma
        registrar("andrea");
        String token = login("andrea", CLAVE);
        int inicioFirma = token.lastIndexOf('.') + 1;
        String alterado = token.substring(0, inicioFirma)
                + (token.charAt(inicioFirma) == 'A' ? 'B' : 'A')
                + token.substring(inicioFirma + 1);

        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    // ---------- apoyo ----------

    private void registrar(String username) throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro(username)))
                .andExpect(status().isCreated());
    }

    private String login(String identifier, String password) throws Exception {
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(identifier, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(json, "$.accessToken");
    }

    private String registro(String username) {
        return """
                {
                  "username": "%s",
                  "email": "%s@email.com",
                  "firstName": "Nombre",
                  "lastName": "Apellido",
                  "phone": "3001234567",
                  "city": "Santa Marta",
                  "birthDate": "2000-05-10",
                  "password": "%s"
                }
                """.formatted(username, username, CLAVE);
    }

    private String credenciales(String identifier, String password) {
        return """
                { "identifier": "%s", "password": "%s" }
                """.formatted(identifier, password);
    }
}
