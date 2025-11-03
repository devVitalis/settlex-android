package com.settlex.android.utils.event;

/**
 * Generic wrapper for results.
 */
public class Result<T> {

    public enum Status {
        LOADING,
        PENDING,
        SUCCESS,
        FAILURE
    }

    private final Status status;
    private final T data;
    private final String message;
    private final String error;

    /**
     * Private constructor to enforce the use of static factory methods.
     * This ensures that a Result object can only be created in a valid state.
     *
     * @param status  The status of the result.
     * @param data    The data payload (null for LOADING, PENDING, ERROR).
     * @param message The associated message (null for SUCCESS, often set for PENDING, ERROR).
     */
    private Result(Status status, T data, String message, String error) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null, null);
    }

    public static <T> Result<T> pending(String message) {
        return new Result<>(Status.PENDING, null, message, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, null, null);
    }

    public static <T> Result<T> failure(String error) {
        return new Result<>(Status.FAILURE, null, null, error);
    }

    // Getters ====
    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}