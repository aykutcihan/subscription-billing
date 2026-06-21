package com.cadence.auth.exception;

import com.cadence.auth.exception.message.Messages;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super(String.format(Messages.USERNAME_ALREADY_TAKEN, username));
    }
}
