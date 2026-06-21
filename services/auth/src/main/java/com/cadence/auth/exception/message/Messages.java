package com.cadence.auth.exception.message;

public final class Messages {

    private Messages() {}

    public static final String USERNAME_ALREADY_TAKEN     = "Username '%s' is already taken";
    public static final String EMAIL_ALREADY_REGISTERED   = "Email '%s' is already registered";
    public static final String INVALID_CREDENTIALS        = "Invalid username or password";
    public static final String VALIDATION_FAILED          = "Validation failed";
    public static final String REFRESH_TOKEN_NOT_FOUND    = "Refresh token not found";
    public static final String REFRESH_TOKEN_EXPIRED      = "Refresh token expired";
    public static final String USER_NOT_FOUND_FOR_REFRESH = "User not found for refresh token";
}
