package com.pulsepass.service.impl;

import com.pulsepass.domain.Venue;
import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.VenueMapper;
import com.pulsepass.repository.VenueRepository;
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
class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueServiceImpl venueService;

    @Test
    @DisplayName("FR-SVC-001: venue existente retorna DTO")
    void venueExistenteRetornaDto() {
        // ARRANGE
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 1 # 2-3", 3);
        VenueResponse esperado = respuestaDe(venue);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(esperado);

        // ACT
        VenueResponse resultado = venueService.findByCode("VEN-SMR-01");

        // ASSERT
        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    @DisplayName("BR-VENUE-001: venue inexistente lanza ResourceNotFoundException")
    void venueInexistenteLanzaResourceNotFound() {
        // ARRANGE
        when(venueRepository.findByCode("VEN-XXX")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> venueService.findByCode("VEN-XXX"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Venue not found: VEN-XXX");

        verify(venueMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("BR-VENUE-002: findActiveVenues retorna los venues activos como DTO")
    void venuesActivosSeRetornanComoDto() {
        // ARRANGE
        Venue marina = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 1 # 2-3", 3);
        Venue teatro = new Venue("VEN-SMR-02", "Teatro Santa Marta", "Santa Marta", "Carrera 4 # 5-6", 500);
        when(venueRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(marina, teatro));
        when(venueMapper.toResponse(marina)).thenReturn(respuestaDe(marina));
        when(venueMapper.toResponse(teatro)).thenReturn(respuestaDe(teatro));

        // ACT
        List<VenueResponse> resultado = venueService.findActiveVenues();

        // ASSERT
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).code()).isEqualTo("VEN-SMR-01");
        assertThat(resultado.get(1).code()).isEqualTo("VEN-SMR-02");
        verify(venueRepository).findByActiveTrueOrderByNameAsc();
    }

    private VenueResponse respuestaDe(Venue venue) {
        return new VenueResponse(
                venue.getId(), venue.getCode(), venue.getName(), venue.getCity(),
                venue.getAddress(), venue.getCapacity(), venue.getActive()
        );
    }
}
