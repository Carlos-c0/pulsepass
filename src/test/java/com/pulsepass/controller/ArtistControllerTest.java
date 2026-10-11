package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.ArtistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArtistController.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService artistService;

    @Test
    @DisplayName("TEST-CTRL-ART-001: buscar artista por ID retorna 200")
    void buscarPorIdRetorna200() throws Exception {
        // ARRANGE
        when(artistService.findById(1L)).thenReturn(solarBeat());

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"))
                .andExpect(jsonPath("$.genre").value("Electronic"));

        verify(artistService).findById(1L);
    }

    @Test
    @DisplayName("TEST-CTRL-ART-002: ID inexistente retorna 404")
    void idInexistenteRetorna404() throws Exception {
        // ARRANGE
        when(artistService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Artist not found: 99"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Artist not found: 99"));
    }

    @Test
    @DisplayName("TEST-CTRL-ART-003: buscar por stageName retorna 200")
    void buscarPorStageNameRetorna200() throws Exception {
        // ARRANGE
        when(artistService.findByStageName("Solar Beat")).thenReturn(solarBeat());

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findByStageName("Solar Beat");
    }

    @Test
    @DisplayName("TEST-CTRL-ART-004: listar artistas activos retorna 200")
    void listarActivosRetorna200() throws Exception {
        // ARRANGE
        ArtistResponse neonWaves = new ArtistResponse(2L, "Neon Waves", "Mexico", "Synth Pop", true);
        when(artistService.findActiveArtists()).thenReturn(List.of(neonWaves, solarBeat()));

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].stageName").value("Neon Waves"))
                .andExpect(jsonPath("$[1].stageName").value("Solar Beat"));

        verify(artistService).findActiveArtists();
    }

    @Test
    @DisplayName("ID que no es numero retorna 400 y no llama al Service")
    void idNoNumericoRetorna400() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter: id"));

        verify(artistService, never()).findById(any());
    }

    private ArtistResponse solarBeat() {
        return new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
    }
}
