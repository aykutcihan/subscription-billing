package com.cadence.subscription.service;

import com.cadence.subscription.domain.Plan;
import com.cadence.subscription.dto.mappers.PlanMapper;
import com.cadence.subscription.dto.response.PlanResponse;
import com.cadence.subscription.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(planMapper::mapPlanToPlanResponse)
                .toList();
    }
}
