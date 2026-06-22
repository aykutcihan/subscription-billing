package com.cadence.subscription.dto.request;

import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
        @NotNull Long planId
) {
}
