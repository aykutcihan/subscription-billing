package com.cadence.subscription.service;

import com.cadence.subscription.domain.BillingCycle;
import com.cadence.subscription.domain.Plan;
import com.cadence.subscription.domain.Subscription;
import com.cadence.subscription.domain.SubscriptionStatus;
import com.cadence.subscription.dto.mappers.SubscriptionMapper;
import com.cadence.subscription.dto.request.SubscribeRequest;
import com.cadence.subscription.dto.response.SubscriptionResponse;
import com.cadence.subscription.exception.PlanNotFoundException;
import com.cadence.subscription.exception.SubscriptionAlreadyActiveException;
import com.cadence.subscription.repository.PlanRepository;
import com.cadence.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public SubscriptionResponse subscribe(Long userId, SubscribeRequest request) {
        subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new SubscriptionAlreadyActiveException(userId);
                });

        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new PlanNotFoundException(request.planId()));

        LocalDate startedAt = LocalDate.now();
        LocalDate nextBillingDate = calculateNextBillingDate(startedAt, plan.getBillingCycle());

        Subscription subscription = subscriptionMapper.mapSubscribeRequestToSubscription(
                request, userId, plan, startedAt, nextBillingDate);

        Subscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.mapSubscriptionToSubscriptionResponse(saved);
    }

    private LocalDate calculateNextBillingDate(LocalDate startedAt, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? startedAt.plusMonths(1)
                : startedAt.plusYears(1);
    }
}
