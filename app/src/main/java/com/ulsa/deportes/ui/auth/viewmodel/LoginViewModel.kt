package com.ulsa.deportes.ui.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.R
import com.ulsa.deportes.ui.auth.data.SessionPreferences
import com.ulsa.deportes.ui.auth.model.GraphQLRequest
import com.ulsa.deportes.ui.auth.model.LoginData
import com.ulsa.deportes.ui.auth.model.LoginUiState
import com.ulsa.deportes.ui.auth.network.AuthRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

/**
 * ViewModel de la pantalla de login. Dueño del estado ([LoginUiState]) y de la
 * lógica: llamar a la API GraphQL, guardar la sesión y traducir los errores a
 * mensajes.
 *
 * Extiende [AndroidViewModel] para pasarle el `Context` a [SessionPreferences]
 * sin arriesgar fugas de memoria (mismo criterio que `OnboardingViewModel`).
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionPreferences(application)
    private val authService = AuthRetrofitClient.authService

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** La mutation de login, como texto — se manda tal cual dentro del [GraphQLRequest]. */
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

    /**
     * Intenta iniciar sesión. Flujo:
     * 1. Valida que los campos no estén vacíos.
     * 2. `isLoading = true`, limpia error previo.
     * 3. `POST /` al gateway con la mutation `login` (GraphQL: una sola URL para todo).
     * 4. Si vienen `errors` en la respuesta (ej. "Credenciales inválidas."), se
     *    muestra ese mensaje — GraphQL casi siempre responde HTTP 200 aunque la
     *    operación falle, así que el error NO llega como `HttpException`.
     * 5. Si viene `data`, guarda el `token` (se usa igual para "access" y
     *    "refresh", ya que este backend no distingue entre ambos) + el usuario,
     *    y pone `isLoggedIn = true`.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = getApplication<Application>().getString(R.string.login_error_empty_fields)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = GraphQLRequest(
                    query = loginMutation,
                    variables = mapOf("email" to email.trim(), "password" to password)
                )
                val response = authService.graphql(request)

                val serverError = response.errors?.firstOrNull()?.message
                if (serverError != null) {
                    _uiState.update { it.copy(isLoading = false, error = mapServerError(serverError)) }
                    return@launch
                }

                val payload = response.data?.login
                if (payload == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = getApplication<Application>().getString(R.string.login_error_unexpected_response)
                        )
                    }
                    return@launch
                }

                session.saveSession(payload.token, payload.token, payload.usuario)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: SocketTimeoutException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = getApplication<Application>().getString(R.string.login_error_server_not_running)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = getApplication<Application>().getString(R.string.login_error_no_connection)
                    )
                }
            }
        }
    }

    /** Limpia el mensaje de error (p. ej. cuando el usuario vuelve a escribir). */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Traduce el mensaje de error que regresa GraphQL (texto libre, definido en
     * el resolver de `seguridad-service`) a algo consistente para el usuario.
     */
    private fun mapServerError(message: String): String = when {
        message.contains("Credenciales inválidas", ignoreCase = true) ->
            getApplication<Application>().getString(R.string.login_error_invalid_credentials)
        message.contains("deshabilitado", ignoreCase = true) ->
            getApplication<Application>().getString(R.string.login_error_disabled_account)
        else -> message
    }
}