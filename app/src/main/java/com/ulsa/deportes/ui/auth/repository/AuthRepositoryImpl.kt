package com.ulsa.deportes.ui.auth.repository

import com.ulsa.deportes.ui.auth.model.GraphQLRequest
import com.ulsa.deportes.ui.auth.model.GraphQLResponse
import com.ulsa.deportes.ui.auth.model.LoginData
import com.ulsa.deportes.ui.auth.model.LoginPayload
import com.ulsa.deportes.ui.auth.model.RegisterData
import com.ulsa.deportes.ui.auth.model.RegisterPayload
import com.ulsa.deportes.ui.auth.model.RegisterVariables
import com.ulsa.deportes.ui.auth.network.AuthRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl : AuthRepository {

    private val authService = AuthRetrofitClient.authService

    private val loginMutation = """
        mutation Login(${'$'}email: String!, ${'$'}password: String!) {
            login(email: ${'$'}email, password: ${'$'}password) {
                token
                usuario {
                    id
                    matricula
                    nombre
                    email
                    rol
                    activo
                }
            }
        }
    """.trimIndent()

    private val registerMutation = """
        mutation RegistrarUsuario(${'$'}input: RegistrarUsuarioInput!) {
            registrarUsuario(input: ${'$'}input) {
                token
                usuario {
                    id
                    matricula
                    nombre
                    email
                    rol
                    activo
                }
            }
        }
    """.trimIndent()

    override suspend fun login(email: String, password: String): Result<LoginPayload> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                query = loginMutation,
                variables = mapOf("email" to email.trim(), "password" to password)
            )
            val response = authService.graphql<LoginData>(request)
            handleResponse(response) { it.login }
        } catch (e: java.net.SocketTimeoutException) {
            Result.NetworkError
        } catch (e: Exception) {
            Result.Error("Sin conexión. Revisa tu red e intenta de nuevo.")
        }
    }

    override suspend fun register(variables: RegisterVariables): Result<RegisterPayload> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                query = registerMutation,
                variables = mapOf("input" to variables)
            )
            val response = authService.graphql<RegisterData>(request)
            handleResponse(response) { it.registrarUsuario }
        } catch (e: java.net.SocketTimeoutException) {
            Result.NetworkError
        } catch (e: Exception) {
            Result.Error("Sin conexión. Revisa tu red e intenta de nuevo.")
        }
    }

    private inline fun <T, R> handleResponse(
        response: GraphQLResponse<T>,
        crossinline extract: (T) -> R
    ): Result<R> {
        val serverError = response.errors?.firstOrNull()?.message
        if (serverError != null) {
            return Result.Error(mapServerError(serverError))
        }
        val data = response.data
        if (data == null) {
            return Result.Error("Respuesta inesperada del servidor.")
        }
        return Result.Success(extract(data))
    }

    private fun mapServerError(message: String): String = when {
        message.contains("Credenciales inválidas", ignoreCase = true) -> "Correo o contraseña incorrectos"
        message.contains("deshabilitado", ignoreCase = true) -> "Esta cuenta está deshabilitada"
        message.contains("ya existe", ignoreCase = true) -> "El correo o matrícula ya están registrados"
        message.contains("email", ignoreCase = true) -> "El correo electrónico no es válido"
        message.contains("matrícula", ignoreCase = true) -> "La matrícula no es válida"
        message.contains("contraseña", ignoreCase = true) -> "La contraseña no cumple los requisitos"
        else -> message
    }
}