package com.kiranawala.domain.models

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    object Loading : Result<Nothing>()
}