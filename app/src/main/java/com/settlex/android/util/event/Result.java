package com.settlex.android.util.event;

/**
 * A generic wrapper class used to communicate the status and data of an asynchronous operation
 * (like an API call) between the ViewModel and the UI (Activity/Fragment).
 * * It helps enforce a clear separation of concerns and handle various states gracefully.
 *
 * @param <T> The type of the data payload contained in a successful result.
 */
public class Result<T> {
    private static final String ERROR_NO_INTERNET = "Connection lost. Please check your Wi-Fi or cellular data and try again";

    /**
     * Defines the possible states of an asynchronous operation.
     */
    public enum Status {
        /** The operation failed due to an active network disconnection. */
        NO_INTERNET,
        /** The operation is currently in progress. */
        LOADING,
        /** The operation completed successfully and may contain data. */
        SUCCESS,
        /** The operation failed due to a server error, validation error, or other internal issue. */
        FAILURE
    }

    private final Status status;
    private final T data;
    private final String message;
    private final String error;

    /**
     * Private constructor to enforce the use of static factory methods.
     * This ensures that a Result object can only be created in a valid and defined state.
     *
     * @param status  The current status of the operation (e.g., LOADING, SUCCESS, FAILURE).
     * @param data    The successful data payload (only non-null for SUCCESS status).
     * @param message A contextual message for statuses like PENDING.
     * @param error   A specific error description for the FAILURE status.
     */
    private Result(Status status, T data, String message, String error) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    /**
     * Creates a Result object representing a network connectivity failure.
     * Automatically sets the standard {@link #ERROR_NO_INTERNET} message.
     *
     * @param <T> The type of the expected data (will be null).
     * @return A Result with status NO_INTERNET.
     */
    public static <T> Result<T> noInternet(){
        return new Result<>(Status.NO_INTERNET, null, ERROR_NO_INTERNET, null);
    }

    /**
     * Creates a Result object representing an active loading state.
     *
     * @param <T> The type of the expected data (will be null).
     * @return A Result with status LOADING.
     */
    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null, null);
    }

    /**
     * Creates a Result object representing a successful operation.
     *
     * @param <T> The type of the data payload.
     * @param data The successful data payload.
     * @return A Result with status SUCCESS and the data.
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, null, null);
    }

    /**
     * Creates a Result object representing a failure in the operation (not due to network).
     *
     * @param <T> The type of the expected data (will be null).
     * @param error The specific error message to be displayed to the user.
     * @return A Result with status FAILURE.
     */
    public static <T> Result<T> failure(String error) {
        return new Result<>(Status.FAILURE, null, null, error);
    }

    // Getters
    /**
     * Retrieves the current status of the asynchronous operation.
     * @return The {@link Status} (e.g., SUCCESS, LOADING).
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Retrieves the data payload, typically only valid when the status is {@link Status#SUCCESS}.
     * @return The data payload of type T, or null if not available.
     */
    public T getData() {
        return data;
    }

    /**
     * Retrieves a contextual message, typically used for {@link Status#NO_INTERNET}.
     * @return A descriptive message, or null.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the specific error description, typically only valid when the status is {@link Status#FAILURE}.
     * @return The error message, or null.
     */
    public String getError() {
        return error;
    }
}