package com.cadence.subscription.dto.response;

import com.cadence.subscription.domain.SubscriptionStatus;
import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        Long userId,
        PlanResponse plan,
        SubscriptionStatus status,
        LocalDate startedAt,
        LocalDate nextBillingDate
) {
}
