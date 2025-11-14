package com.settlex.android.util.event;

/**
 * Wrapper for data that is exposed via LiveData representing a single-use event.
 * Ensures the content is consumed only once, such as navigation triggers or toast messages.
 *
 * @param <T> The type of content held.
 */
public class Event<T> {
    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns the content if it has not been handled yet, and marks it as handled.
     *
     * @return The unhandled content or null if already handled.
     */
    public T getContentIfNotHandled() {
        if (handled) return null;
        handled = true;
        return content;
    }

    /**
     * Returns the content regardless of whether it has been handled.
     */
    public T peekContent() {
        return content;
    }
}
