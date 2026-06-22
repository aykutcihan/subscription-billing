package com.cadence.subscription.dto.mappers;

import com.cadence.subscription.domain.Plan;
import com.cadence.subscription.domain.Subscription;
import com.cadence.subscription.domain.SubscriptionStatus;
import com.cadence.subscription.dto.request.SubscribeRequest;
import com.cadence.subscription.dto.response.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {

    private final PlanMapper planMapper;

    public Subscription mapSubscribeRequestToSubscription(
            SubscribeRequest request, Long userId, Plan plan, LocalDate startedAt, LocalDate nextBillingDate) {
        return Subscription.builder()
                .userId(userId)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(startedAt)
                .nextBillingDate(nextBillingDate)
                .build();
    }

    public SubscriptionResponse mapSubscriptionToSubscriptionResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUserId(),
                planMapper.mapPlanToPlanResponse(subscription.getPlan()),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getNextBillingDate()
        );
    }
}
