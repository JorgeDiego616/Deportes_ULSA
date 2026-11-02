package com.ulsa.deportes.ui.auth.domain

import com.ulsa.deportes.ui.auth.model.UserDto
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    data class LoginResult(
        val token: String,
        val user: UserDto
    )

    data class RegisterResult(
        val token: String,
        val user: UserDto
    )

    sealed class AuthError : Exception() {
        override abstract val message: String
        abstract val fieldErrors: Map<String, String>

        data class InvalidCredentials(override val message: String) : AuthError() {
            override val fieldErrors: Map<String, String> = emptyMap()
        }
        data class AccountDisabled(override val message: String) : AuthError() {
            override val fieldErrors: Map<String, String> = emptyMap()
        }
        data class NetworkError(override val message: String) : AuthError() {
            override val fieldErrors: Map<String, String> = emptyMap()
        }
        data class ServerError(
            override val message: String,
            override val fieldErrors: Map<String, String>
        ) : AuthError()
        data class UnknownError(override val message: String) : AuthError() {
            override val fieldErrors: Map<String, String> = emptyMap()
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResult>

    suspend fun register(
        matricula: String,
        nombre: String,
        email: String,
        password: String
    ): Result<RegisterResult>

    val isLoggedIn: Flow<Boolean>
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    suspend fun logout()
}