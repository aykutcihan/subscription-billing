package com.cadence.auth.exception;

import com.cadence.auth.exception.message.Messages;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(String.format(Messages.EMAIL_ALREADY_REGISTERED, email));
    }
}
