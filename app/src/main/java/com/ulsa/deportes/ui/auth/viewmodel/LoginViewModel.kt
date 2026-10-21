package com.ulsa.deportes.ui.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.ui.auth.data.SessionPreferences
import com.ulsa.deportes.ui.auth.model.LoginUiState
import com.ulsa.deportes.ui.auth.repository.AuthRepository
import com.ulsa.deportes.ui.auth.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de login. Dueño del estado ([LoginUiState]) y de la
 * lógica: autenticar vía [AuthRepository], guardar la sesión y traducir los errores a
 * mensajes.
 *
 * Extiende [AndroidViewModel] para pasarle el `Context` a [SessionPreferences]
 * sin arriesgar fugas de memoria (mismo criterio que `OnboardingViewModel`).
 */
class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val session = SessionPreferences(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Intenta iniciar sesión. Flujo:
     * 1. Valida que los campos no estén vacíos.
     * 2. `isLoading = true`, limpia error previo.
     * 3. Llama a [AuthRepository.login].
     * 4. Si hay error, lo muestra.
     * 5. Si éxito, guarda el token + usuario y pone `isLoggedIn = true`.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa correo y contraseña") }
            return
        }

        // Evitar solicitudes duplicadas si ya hay una en curso
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(email, password)
            when (result) {
                is Result.Success -> {
                    val payload = result.value
                    session.saveSession(payload.token, payload.token, payload.usuario)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                Result.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No se pudo conectar al servidor. Verifica que esté corriendo.")
                    }
                }
            }
        }
    }

    /** Limpia el mensaje de error (p. ej. cuando el usuario vuelve a escribir). */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(private val application: Application, private val authRepository: AuthRepository) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(application, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}