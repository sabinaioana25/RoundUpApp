package com.example.roundupapp.ui.home

import com.example.roundupapp.domain.ValidationException
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.utils.Constants.HOME_SCREEN_OFFLINE_ERROR
import java.io.IOException

/**
 * Maps domain exceptions to UI error types
 */
object ErrorMapper {
    fun mapToUiError(
        exception: Throwable?,
        defaultMessage: String,
        operation: LoadingState.Operation? = null
    ): UiError {
        return when (exception) {
            is ValidationException -> UiError.ValidationError(
                exception.message ?: defaultMessage
            )
            is OfflineException -> UiError.OfflineError(
                exception.message ?: HOME_SCREEN_OFFLINE_ERROR
            )
            is IOException -> UiError.NetworkError(defaultMessage)
            else -> if (operation != null) {
                UiError.OperationError(defaultMessage, operation)
            } else {
                UiError.DataError(defaultMessage)
            }
        }
    }

    fun <T> Result<T>.toUiError(
        defaultMessage: String,
        operation: LoadingState.Operation? = null
    ): UiError {
        return mapToUiError(
            exception = exceptionOrNull(),
            defaultMessage = defaultMessage,
            operation = operation
        )
    }
}
