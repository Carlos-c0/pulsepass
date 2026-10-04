package com.pulsepass.service.impl;

import com.pulsepass.domain.TicketType;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.service.TicketPricingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DefaultTicketPricingStrategy implements TicketPricingStrategy {

    private static final BigDecimal BASE_PRICE = new BigDecimal("100000.00");

    private static final BigDecimal GENERAL_MULTIPLIER = new BigDecimal("1.00");
    private static final BigDecimal STUDENT_MULTIPLIER = new BigDecimal("0.80");
    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("2.00");
    private static final BigDecimal BACKSTAGE_MULTIPLIER = new BigDecimal("3.00");

    @Override
    public BigDecimal calculatePrice(TicketType type) {
        if (type == null) {
            throw new BusinessRuleException("Ticket type is required.");
        }

        BigDecimal multiplier = switch (type) {
            case GENERAL -> GENERAL_MULTIPLIER;
            case STUDENT -> STUDENT_MULTIPLIER;
            case VIP -> VIP_MULTIPLIER;
            case BACKSTAGE -> BACKSTAGE_MULTIPLIER;
        };

        return BASE_PRICE.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
