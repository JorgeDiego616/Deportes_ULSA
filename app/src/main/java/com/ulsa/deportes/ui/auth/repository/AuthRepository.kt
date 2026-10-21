package com.ulsa.deportes.ui.auth.repository

import com.ulsa.deportes.ui.auth.model.LoginPayload
import com.ulsa.deportes.ui.auth.model.RegisterPayload
import com.ulsa.deportes.ui.auth.model.RegisterVariables

sealed interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginPayload>
    suspend fun register(variables: RegisterVariables): Result<RegisterPayload>
}

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Error(val message: String, val fieldErrors: Map<String, String> = emptyMap()) : Result<Nothing>()
    object NetworkError : Result<Nothing>()
}