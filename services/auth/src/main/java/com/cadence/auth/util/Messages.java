package com.cadence.auth.util;

public final class Messages {

    private Messages() {}

    // --- Hata mesajları ---
    public static final String USERNAME_ALREADY_TAKEN     = "Username '%s' is already taken";
    public static final String EMAIL_ALREADY_REGISTERED   = "Email '%s' is already registered";
    public static final String INVALID_CREDENTIALS        = "Invalid username or password";
    public static final String VALIDATION_FAILED          = "Validation failed";
    public static final String REFRESH_TOKEN_NOT_FOUND    = "Refresh token not found";
    public static final String REFRESH_TOKEN_EXPIRED      = "Refresh token expired";
    public static final String USER_NOT_FOUND_FOR_REFRESH = "User not found for refresh token";

    // --- Başarı mesajları ---
    public static final String USER_REGISTERED = "User registered successfully";
    public static final String LOGIN_SUCCESS   = "Login successful";
    public static final String TOKEN_REFRESHED = "Token refreshed successfully";
    public static final String LOGOUT_SUCCESS  = "Logged out successfully";
}
