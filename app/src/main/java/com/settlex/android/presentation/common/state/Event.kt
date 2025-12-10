package com.settlex.android.presentation.common.state

class Event<out T>(private val data: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? {
        return when {
            handled -> null
            else -> {
                handled = true
                data
            }
        }
    }

    fun peekContent(): T {
        return data
    }
}