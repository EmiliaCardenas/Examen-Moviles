package com.example.examen.domain.common

sealed class Result<out T> {
    /** Represents a loading or in-progress state. */
    object Loading : Result<Nothing>()

    /** Represents a successful result containing [data]. */
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    /** Represents a failed result containing an [exception]. */
    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()
}