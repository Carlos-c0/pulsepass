package com.pulsepass.service.impl;

import com.pulsepass.domain.TicketType;
import com.pulsepass.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultTicketPricingStrategyTest {

    private final DefaultTicketPricingStrategy strategy = new DefaultTicketPricingStrategy();

    @Test
    @DisplayName("GENERAL paga el precio base")
    void generalPagaElPrecioBase() {
        assertThat(strategy.calculatePrice(TicketType.GENERAL)).isEqualByComparingTo("100000.00");
    }

    @Test
    @DisplayName("STUDENT paga el precio base con 20% de descuento")
    void studentPagaConDescuento() {
        assertThat(strategy.calculatePrice(TicketType.STUDENT)).isEqualByComparingTo("80000.00");
    }

    @Test
    @DisplayName("VIP paga el doble del precio base")
    void vipPagaElDoble() {
        assertThat(strategy.calculatePrice(TicketType.VIP)).isEqualByComparingTo("200000.00");
    }

    @Test
    @DisplayName("BACKSTAGE paga el triple del precio base")
    void backstagePagaElTriple() {
        assertThat(strategy.calculatePrice(TicketType.BACKSTAGE)).isEqualByComparingTo("300000.00");
    }

    @Test
    @DisplayName("BR-TICKET-009: ningun tipo produce un precio negativo y todos usan 2 decimales")
    void ningunTipoProducePrecioNegativo() {
        for (TicketType type : TicketType.values()) {
            BigDecimal price = strategy.calculatePrice(type);

            assertThat(price).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(price.scale()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Un tipo nulo lanza BusinessRuleException")
    void tipoNuloLanzaBusinessRule() {
        assertThatThrownBy(() -> strategy.calculatePrice(null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Ticket type is required.");
    }
}
