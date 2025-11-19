package com.settlex.android.ui.common.event

/**
 * A sealed class representing the state of an operation, typically an asynchronous one like a network request.
 * It can be in one of three states: [Success], [Failure], or [Loading].
 *
 * @param T The type of data held by the [Success] state.
 */
sealed class UiState<out T> {

    data class Success<out T>(val data: T?) : UiState<T>()
    data class Failure(val message: String?) : UiState<Nothing>()
    object Loading : UiState<Nothing>()

    fun toData(): T? =
        if (this is Success) this.data else null
}