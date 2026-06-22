package com.cadence.subscription.api;

import com.cadence.subscription.dto.request.SubscribeRequest;
import com.cadence.subscription.dto.response.ApiResult;
import com.cadence.subscription.dto.response.SubscriptionResponse;
import com.cadence.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResult<SubscriptionResponse>> subscribe(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SubscribeRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response, "Subscribed successfully"));
    }
}
