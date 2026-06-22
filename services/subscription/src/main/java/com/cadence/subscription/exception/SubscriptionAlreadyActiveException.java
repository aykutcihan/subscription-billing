package com.cadence.subscription.exception;

import com.cadence.subscription.exception.message.Messages;

public class SubscriptionAlreadyActiveException extends RuntimeException {

    public SubscriptionAlreadyActiveException(Long userId) {
        super(String.format(Messages.SUBSCRIPTION_ALREADY_ACTIVE, userId));
    }
}
