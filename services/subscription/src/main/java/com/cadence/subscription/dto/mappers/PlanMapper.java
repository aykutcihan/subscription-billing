package com.cadence.subscription.dto.mappers;

import com.cadence.subscription.domain.Plan;
import com.cadence.subscription.dto.response.PlanResponse;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {

    public PlanResponse mapPlanToPlanResponse(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getBillingCycle(),
                plan.getPrice()
        );
    }
}
