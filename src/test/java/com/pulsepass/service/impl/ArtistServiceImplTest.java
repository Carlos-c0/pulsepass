package com.pulsepass.service.impl;

import com.pulsepass.domain.Artist;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.repository.ArtistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistMapper artistMapper;

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Test
    @DisplayName("FR-SVC-009: artista existente por id retorna DTO")
    void artistaExistentePorIdRetornaDto() {

        Artist artista = new Artist("Solar Beat", "Colombia", "Electronic");
        ArtistResponse esperado = respuestaDe(artista);
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artista));
        when(artistMapper.toResponse(artista)).thenReturn(esperado);

        ArtistResponse resultado = artistService.findById(1L);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    @DisplayName("BR-ARTIST-001: artista inexistente por id lanza ResourceNotFoundException")
    void artistaInexistentePorIdLanzaResourceNotFound() {
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artist not found: 99");

        verify(artistMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("FR-SVC-009: artista por nombre artistico se busca sin distinguir mayusculas")
    void artistaPorNombreArtisticoRetornaDto() {
        Artist artista = new Artist("Neon Waves", "Mexico", "Synth Pop");
        ArtistResponse esperado = respuestaDe(artista);
        when(artistRepository.findByStageNameIgnoreCase("neon waves")).thenReturn(Optional.of(artista));
        when(artistMapper.toResponse(artista)).thenReturn(esperado);

        ArtistResponse resultado = artistService.findByStageName("neon waves");

        assertThat(resultado.stageName()).isEqualTo("Neon Waves");
    }

    @Test
    @DisplayName("BR-ARTIST-001: artista inexistente por nombre lanza ResourceNotFoundException")
    void artistaInexistentePorNombreLanzaResourceNotFound() {
        when(artistRepository.findByStageNameIgnoreCase("Nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findByStageName("Nadie"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artist not found: Nadie");
    }

    @Test
    @DisplayName("BR-ARTIST-002: findActiveArtists retorna los artistas activos como DTO")
    void artistasActivosSeRetornanComoDto() {
        Artist caribbean = new Artist("Caribbean Sound", "Colombia", "Reggae");
        Artist solar = new Artist("Solar Beat", "Colombia", "Electronic");
        when(artistRepository.findByActiveTrueOrderByStageNameAsc()).thenReturn(List.of(caribbean, solar));
        when(artistMapper.toResponse(caribbean)).thenReturn(respuestaDe(caribbean));
        when(artistMapper.toResponse(solar)).thenReturn(respuestaDe(solar));

        List<ArtistResponse> resultado = artistService.findActiveArtists();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).stageName()).isEqualTo("Caribbean Sound");
        assertThat(resultado.get(1).stageName()).isEqualTo("Solar Beat");
    }

    private ArtistResponse respuestaDe(Artist artista) {
        return new ArtistResponse(
                artista.getId(), artista.getStageName(), artista.getCountry(),
                artista.getGenre(), artista.getActive()
        );
    }
}
