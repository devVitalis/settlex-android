package com.settlex.android.ui.auth.util;

public record AuthResult(boolean isSuccess, boolean exists, String message) {

    // For generic success
    public static AuthResult success(String message) {
        return new AuthResult(true, false, message);
    }

    // For existence check
    public static AuthResult exists(boolean exists) {
        return new AuthResult(true, exists, "");
    }

    // For generic failure
    public static AuthResult failure(String message) {
        return new AuthResult(false, false, message);
    }
}
