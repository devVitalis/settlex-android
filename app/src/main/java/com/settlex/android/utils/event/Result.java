package com.settlex.android.utils.event;

/**
 * Generic wrapper for results.
 * Encapsulates the operation status (loading, success, error, pending),
 * optional data payload, and an optional message for errors or status updates.
 *
 * @param <T> The type of data returned on success.
 */
public class Result<T> {

    /**
     * Defines the possible states of the operation.
     */
    public enum Status {
        LOADING,
        PENDING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data;
    private final String message;

    /**
     * Private constructor to enforce the use of static factory methods.
     * This ensures that a Result object can only be created in a valid state.
     *
     * @param status  The status of the result.
     * @param data    The data payload (null for LOADING, PENDING, ERROR).
     * @param message The associated message (null for SUCCESS, often set for PENDING, ERROR).
     */
    private Result(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }


    /**
     * Factory method for creating a Result in the LOADING state.
     * The data and message will be null.
     *
     * @param <T> The generic type of the data.
     * @return A new Result instance with Status.LOADING.
     */
    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null);
    }

    /**
     * Factory method for creating a Result in the PENDING state.
     * This state is useful for operations that are paused or require further action.
     * The data will be null.
     *
     * @param <T>     The generic type of the data.
     * @param message A message describing what is pending (e.g., "Awaiting user confirmation").
     * @return A new Result instance with Status.PENDING.
     */
    public static <T> Result<T> Pending(String message) {
        return new Result<>(Status.PENDING, null, message);
    }

    /**
     * Factory method for creating a Result in the SUCCESS state.
     * The message will be null.
     *
     * @param <T>  The generic type of the data.
     * @param data The successfully retrieved data.
     * @return A new Result instance with Status.SUCCESS and the data payload.
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, null);
    }

    /**
     * Factory method for creating a Result in the ERROR state.
     * The data will be null.
     *
     * @param <T>     The generic type of the data.
     * @param message A descriptive error message.
     * @return A new Result instance with Status.ERROR and the error message.
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(Status.ERROR, null, message);
    }

    // --- Getters ---

    /**
     * Returns the current status of the operation.
     *
     * @return The Status (LOADING, PENDING, SUCCESS, or ERROR).
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the data payload.
     * This will be non-null only if getStatus() is SUCCESS.
     *
     * @return The data of type T, or null.
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the associated message.
     * This is typically a description for PENDING or ERROR states.
     *
     * @return The message string, or null.
     */
    public String getMessage() {
        return message;
    }
}