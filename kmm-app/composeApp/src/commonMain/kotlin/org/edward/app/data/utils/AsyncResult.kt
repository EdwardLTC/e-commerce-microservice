package org.edward.app.data.utils

sealed interface AsyncResult<out T> {
    data class Success<T>(val data: T) : AsyncResult<T>
    data class Error(val exception: Throwable? = null, val displayMessage: String? = null) :
        AsyncResult<Nothing>

}
