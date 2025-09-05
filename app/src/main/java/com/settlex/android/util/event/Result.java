package com.settlex.android.util.event;

/**
 * Generic wrapper for authentication-related results.
 * Encapsulates the operation status (loading, success, error),
 * optional data payload, and an optional message for errors.
 *
 * @param <T> The type of data returned on success.
 */
public class Result<T> {

    public enum Status {
        LOADING, PENDING, SUCCESS, ERROR
    }

    private final Status status;
    private final T data;
    private final String message;

    private Result(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * Factory method for a loading state.
     */
    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null);
    }

    /**
     * Factory method for a Pending state.
     */
    public static <T> Result<T> Pending() {
        return new Result<>(Status.PENDING, null, null);
    }

    /**
     * Factory method for a success state.
     *
     * @param data The data returned by the operation.
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, null);
    }

    /**
     * Factory method for an error state.
     *
     * @param message Error message describing the failure.
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(Status.ERROR, null, message);
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