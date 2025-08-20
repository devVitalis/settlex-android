package com.settlex.android.ui.auth.util;

/**
 * Generic wrapper for authentication-related results.
 * Encapsulates the operation status (loading, success, error),
 * optional data payload, and an optional message for errors.
 *
 * @param <T> The type of data returned on success.
 */
public class AuthResult<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data;
    private final String message;

    private AuthResult(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * Factory method for a loading state.
     */
    public static <T> AuthResult<T> loading() {
        return new AuthResult<>(Status.LOADING, null, null);
    }

    /**
     * Factory method for a success state.
     * @param data The data returned by the operation.
     */
    public static <T> AuthResult<T> success(T data) {
        return new AuthResult<>(Status.SUCCESS, data, null);
    }

    /**
     * Factory method for an error state.
     * @param message Error message describing the failure.
     */
    public static <T> AuthResult<T> error(String message) {
        return new AuthResult<>(Status.ERROR, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}