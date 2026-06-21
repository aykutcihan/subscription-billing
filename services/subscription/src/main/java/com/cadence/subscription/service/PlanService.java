package com.cadence.subscription.service;

import com.cadence.subscription.domain.Plan;
import com.cadence.subscription.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }
}
