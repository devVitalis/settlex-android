package com.settlex.android.ui.common.state

import com.settlex.android.data.exception.AppException

/**
 * A sealed class representing the state of an operation, typically an asynchronous one like a network request.
 * It can be in one of three states: [Success], [Failure], or [Loading].
 *
 * @param T The type of data held by the [Success] state.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T?) : UiState<T>()
    data class Failure(val exception: AppException) : UiState<Nothing>()
}