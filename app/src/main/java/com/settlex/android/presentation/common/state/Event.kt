package com.settlex.android.presentation.common.state

/**
 * A wrapper class for data that represents a one-time event.
 *
 * Prevents the same event from being re-triggered on configuration changes
 * or when a new observer subscribes.
 *
 * @param T The type of the content.
 * @property content The actual content of the event.
 */
class Event<T> (private val content: T) {

    private var handled = false

    fun getContentIfNotHandled(): T? {
        if (handled) return null
        handled = true
        return content
    }

    fun peekContent(): T? {
        return content
    }
}
