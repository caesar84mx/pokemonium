package com.mdymnoff.pokemonium.data.model.backbone

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val exception: Throwable) : DataResult<Nothing>()
}

inline fun <T> DataResult<T>.onSuccess(action: (value: T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onError(action: (exception: Throwable) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(exception)
    return this
}
