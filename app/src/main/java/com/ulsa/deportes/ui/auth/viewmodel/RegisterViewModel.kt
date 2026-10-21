package com.ulsa.deportes.ui.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.ui.auth.data.SessionPreferences
import com.ulsa.deportes.ui.auth.model.RegisterInput
import com.ulsa.deportes.ui.auth.model.RegisterUiState
import com.ulsa.deportes.ui.auth.model.RegisterVariables
import com.ulsa.deportes.ui.auth.repository.AuthRepository
import com.ulsa.deportes.ui.auth.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val session = SessionPreferences(application)

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(matricula: String, nombre: String, email: String, password: String) {
        // Validaciones básicas
        if (matricula.isBlank() || nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Todos los campos son obligatorios") }
            return
        }
        if (!email.contains("@")) {
            _uiState.update { it.copy(fieldErrors = it.fieldErrors + ("email" to "Correo inválido")) }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(fieldErrors = it.fieldErrors + ("password" to "Mínimo 6 caracteres")) }
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, fieldErrors = emptyMap()) }
            val input = RegisterInput(matricula = matricula.trim(), nombre = nombre.trim(), email = email.trim(), password = password)
            val result = authRepository.register(RegisterVariables(input))
            when (result) {
                is Result.Success -> {
                    val payload = result.value
                    session.saveSession(payload.token, payload.token, payload.usuario)
                    _uiState.update { it.copy(isLoading = false, isRegistered = true) }
                }
                is Result.Error -> {
                    val fieldErrors = result.fieldErrors
                    val generalError = if (fieldErrors.isEmpty()) result.message else null
                    _uiState.update { it.copy(isLoading = false, error = generalError, fieldErrors = fieldErrors) }
                }
                Result.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No se pudo conectar al servidor. Verifica que esté corriendo.")
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearFieldError(field: String) {
        _uiState.update { it.copy(fieldErrors = it.fieldErrors - field) }
    }

    class Factory(private val application: Application, private val authRepository: AuthRepository) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(application, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}