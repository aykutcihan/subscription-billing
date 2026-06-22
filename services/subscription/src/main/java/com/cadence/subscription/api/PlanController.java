package com.cadence.subscription.api;

import com.cadence.subscription.dto.response.ApiResult;
import com.cadence.subscription.dto.response.PlanResponse;
import com.cadence.subscription.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<ApiResult<List<PlanResponse>>> getAllPlans() {
        return ResponseEntity.ok(ApiResult.success(planService.getAllPlans(), "Plans retrieved successfully"));
    }
}
