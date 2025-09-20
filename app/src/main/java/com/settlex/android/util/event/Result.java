package com.settlex.android.util.event;

/**
 * Generic wrapper for results.
 * Encapsulates the operation status (loading, success, error),
 * optional data payload, and an optional message for errors.
 *
 * @param <T> The type of data returned on success.
 */
public class Result<T> {

    public enum Status {
        LOADING,
        PENDING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data;
    private final String message;

    private Result(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }


    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null);
    }

    public static <T> Result<T> Pending(String message) {
        return new Result<>(Status.PENDING, null, message);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(Status.ERROR, null, message);
    }

    // getters
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