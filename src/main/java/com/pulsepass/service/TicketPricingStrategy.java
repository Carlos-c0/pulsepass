package com.pulsepass.service;

import com.pulsepass.domain.TicketType;

import java.math.BigDecimal;

public interface TicketPricingStrategy {

    BigDecimal calculatePrice(TicketType type);
}
