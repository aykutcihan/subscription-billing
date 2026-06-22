package com.cadence.subscription.dto.response;

import com.cadence.subscription.domain.BillingCycle;
import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String name,
        BillingCycle billingCycle,
        BigDecimal price
) {
}
