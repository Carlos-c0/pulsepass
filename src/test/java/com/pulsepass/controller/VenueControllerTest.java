package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @Test
    @DisplayName("TEST-CTRL-VEN-001: venue existente retorna 200 y VenueResponse")
    void venueExistenteRetorna200() throws Exception {
        // ARRANGE
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(marina());

        // ACT + ASSERT
        mockMvc.perform(get("/api/venues/{code}", "VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"))
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.active").value(true));

        verify(venueService).findByCode("VEN-SMR-01");
    }

    @Test
    @DisplayName("TEST-CTRL-VEN-002: venue inexistente retorna 404 con ErrorResponse")
    void venueInexistenteRetorna404() throws Exception {
        // ARRANGE
        when(venueService.findByCode("VEN-XXX"))
                .thenThrow(new ResourceNotFoundException("Venue not found: VEN-XXX"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/venues/{code}", "VEN-XXX"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Venue not found: VEN-XXX"))
                .andExpect(jsonPath("$.details").isEmpty());

        verify(venueService).findByCode("VEN-XXX");
    }

    @Test
    @DisplayName("TEST-CTRL-VEN-003: venues activos retorna 200 con la lista")
    void venuesActivosRetorna200() throws Exception {
        // ARRANGE
        VenueResponse teatro = new VenueResponse(
                2L, "VEN-SMR-02", "Teatro Santa Marta", "Santa Marta", "Carrera 4 # 5-6", 500, true
        );
        when(venueService.findActiveVenues()).thenReturn(List.of(marina(), teatro));

        // ACT + ASSERT
        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[1].code").value("VEN-SMR-02"));

        verify(venueService).findActiveVenues();
    }

    @Test
    @DisplayName("Error inesperado retorna 500 sin exponer detalles internos")
    void errorInesperadoRetorna500SinDetallesInternos() throws Exception {
        // ARRANGE
        when(venueService.findByCode("VEN-SMR-01"))
                .thenThrow(new IllegalStateException("Connection refused: db-host:5432"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/venues/{code}", "VEN-SMR-01"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    private VenueResponse marina() {
        return new VenueResponse(
                1L, "VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 1 # 2-3", 3, true
        );
    }
}
