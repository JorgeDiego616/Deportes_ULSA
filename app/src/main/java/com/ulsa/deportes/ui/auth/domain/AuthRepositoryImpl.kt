package com.ulsa.deportes.ui.auth.domain

import android.app.Application
import com.ulsa.deportes.R
import com.ulsa.deportes.ui.auth.data.SessionPreferences
import com.ulsa.deportes.ui.auth.model.GraphQLRequest
import com.ulsa.deportes.ui.auth.model.LoginData
import com.ulsa.deportes.ui.auth.model.LoginPayload
import com.ulsa.deportes.ui.auth.model.RegisterData
import com.ulsa.deportes.ui.auth.model.UserDto
import com.ulsa.deportes.ui.auth.network.AuthRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(private val application: Application) : AuthRepository {

    private val session = SessionPreferences(application)
    private val authService = AuthRetrofitClient.authService

    private val _isLoggedIn = MutableStateFlow(session.isLoggedIn())
    override val isLoggedIn = _isLoggedIn.asStateFlow()

    private val loginMutation = "mutation Login(\$email: String!, \$password: String!) {\n" +
            "    login(email: \$email, password: \$password) {\n" +
            "        token\n" +
            "        usuario {\n" +
            "            id\n" +
            "            matricula\n" +
            "            nombre\n" +
            "            email\n" +
            "            rol\n" +
            "            activo\n" +
            "        }\n" +
            "    }\n" +
            "}"

    private val registerMutation = "mutation RegistrarUsuario(\$matricula: String!, \$nombre: String!, \$email: String!, \$password: String!, \$rol: String!) {\n" +
            "    registrarUsuario(matricula: \$matricula, nombre: \$nombre, email: \$email, password: \$password, rol: \$rol) {\n" +
            "        id\n" +
            "        matricula\n" +
            "        nombre\n" +
            "        email\n" +
            "        rol\n" +
            "        activo\n" +
            "    }\n" +
            "}"

    private val defaultRole = "ESTUDIANTE"

    override suspend fun login(email: String, password: String): Result<AuthRepository.LoginResult> {
        val request = GraphQLRequest(
            query = loginMutation,
            variables = mapOf("email" to email.trim(), "password" to password)
        )

        return try {
            val response = authService.graphql(request)

            val serverError = response.errors?.firstOrNull()?.message
            if (serverError != null) {
                Result.failure(mapServerError(serverError))
            } else {
                val payload = response.data?.login
                if (payload == null) {
                    Result.failure(AuthRepository.AuthError.UnknownError(application.getString(R.string.auth_error_unexpected_response)))
                } else {
                    session.saveSession(payload.token, payload.token, payload.usuario)
                    _isLoggedIn.value = true
                    Result.success(AuthRepository.LoginResult(payload.token, payload.usuario))
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(AuthRepository.AuthError.NetworkError(application.getString(R.string.auth_error_server_not_running)))
        } catch (e: Exception) {
            Result.failure(AuthRepository.AuthError.NetworkError(application.getString(R.string.auth_error_no_connection)))
        }
    }

    override suspend fun register(
        matricula: String,
        nombre: String,
        email: String,
        password: String
    ): Result<AuthRepository.RegisterResult> {
        val request = GraphQLRequest(
            query = registerMutation,
            variables = mapOf(
                "matricula" to matricula.trim(),
                "nombre" to nombre.trim(),
                "email" to email.trim(),
                "password" to password,
                "rol" to defaultRole
            )
        )

        return try {
            val response = authService.graphqlRegister(request)

            val errors = response.errors
            if (errors != null && errors.isNotEmpty()) {
                val fieldErrors = parseFieldErrors(errors)
                val firstMessage = errors.first().message
                val mappedError = mapServerError(firstMessage)
                Result.failure(AuthRepository.AuthError.ServerError(
                    message = mappedError.message,
                    fieldErrors = fieldErrors
                ))
            } else {
                val usuario = response.data?.registrarUsuario
                if (usuario == null) {
                    Result.failure(AuthRepository.AuthError.UnknownError(application.getString(R.string.auth_error_unexpected_response)))
                } else {
                    login(email, password).fold(
                        onSuccess = {
                            Result.success(AuthRepository.RegisterResult(it.token, it.user))
                        },
                        onFailure = {
                            Result.failure(it)
                        }
                    )
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(AuthRepository.AuthError.NetworkError(application.getString(R.string.auth_error_server_not_running)))
        } catch (e: Exception) {
            Result.failure(AuthRepository.AuthError.NetworkError(application.getString(R.string.auth_error_no_connection)))
        }
    }

    private fun mapServerError(message: String): AuthRepository.AuthError {
        val emailExists = application.getString(R.string.auth_error_email_exists)
        val matriculaExists = application.getString(R.string.auth_error_matricula_exists)
        return when {
            message.contains("Credenciales inválidas", ignoreCase = true) ->
                AuthRepository.AuthError.InvalidCredentials(
                    application.getString(R.string.auth_error_invalid_credentials)
                )
            message.contains("deshabilitado", ignoreCase = true) ->
                AuthRepository.AuthError.AccountDisabled(
                    application.getString(R.string.auth_error_disabled_account)
                )
            message.contains("ya existe", ignoreCase = true) ->
                AuthRepository.AuthError.ServerError(emailExists, mapOf("email" to emailExists))
            message.contains("ya registrado", ignoreCase = true) ->
                AuthRepository.AuthError.ServerError(emailExists, mapOf("email" to emailExists))
            message.contains("matrícula", ignoreCase = true) ->
                AuthRepository.AuthError.ServerError(matriculaExists, mapOf("matricula" to matriculaExists))
            message.contains("matricula", ignoreCase = true) ->
                AuthRepository.AuthError.ServerError(matriculaExists, mapOf("matricula" to matriculaExists))
            else -> AuthRepository.AuthError.UnknownError(message)
        }
    }

    private fun parseFieldErrors(errors: List<com.ulsa.deportes.ui.auth.model.GraphQLError>): Map<String, String> {
        val fieldErrors = mutableMapOf<String, String>()
        for (error in errors) {
            val message = error.message
            when {
                message.contains("email", ignoreCase = true) || message.contains("correo", ignoreCase = true) -> {
                    fieldErrors["email"] = if (fieldErrors.containsKey("email"))
                        "${fieldErrors["email"]}\n$message" else message
                }
                message.contains("matrícula", ignoreCase = true) || message.contains("matricula", ignoreCase = true) -> {
                    fieldErrors["matricula"] = if (fieldErrors.containsKey("matricula"))
                        "${fieldErrors["matricula"]}\n$message" else message
                }
                message.contains("nombre", ignoreCase = true) -> {
                    fieldErrors["nombre"] = if (fieldErrors.containsKey("nombre"))
                        "${fieldErrors["nombre"]}\n$message" else message
                }
                message.contains("password", ignoreCase = true) || message.contains("contraseña", ignoreCase = true) -> {
                    fieldErrors["password"] = if (fieldErrors.containsKey("password"))
                        "${fieldErrors["password"]}\n$message" else message
                }
                else -> {
                    fieldErrors["general"] = if (fieldErrors.containsKey("general"))
                        "${fieldErrors["general"]}\n$message" else message
                }
            }
        }
        return fieldErrors
    }

    override fun getAccessToken(): String? = session.accessToken()

    override fun getRefreshToken(): String? = session.refreshToken()

    override suspend fun logout() {
        session.clearSession()
        _isLoggedIn.value = false
    }
}