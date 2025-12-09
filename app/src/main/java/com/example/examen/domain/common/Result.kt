package com.example.examen.domain.common

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(
        val data: T,
    ) : Result<T>()
    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()
}