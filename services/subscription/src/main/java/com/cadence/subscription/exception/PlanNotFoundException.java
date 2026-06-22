package com.cadence.subscription.exception;

import com.cadence.subscription.exception.message.Messages;

public class PlanNotFoundException extends RuntimeException {

    public PlanNotFoundException(Long planId) {
        super(String.format(Messages.PLAN_NOT_FOUND, planId));
    }
}
