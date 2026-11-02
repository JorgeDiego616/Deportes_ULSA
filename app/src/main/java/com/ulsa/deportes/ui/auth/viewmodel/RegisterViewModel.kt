package com.ulsa.deportes.ui.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.ui.auth.domain.AuthRepository
import com.ulsa.deportes.ui.auth.model.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(matricula: String, nombre: String, email: String, password: String) {
        val validationError = validateFields(matricula, nombre, email, password)
        if (validationError != null) {
            _uiState.update { it.copy(
                isLoading = false,
                generalError = validationError,
                fieldErrors = emptyMap()
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, fieldErrors = emptyMap()) }
            val result = authRepository.register(matricula, nombre, email, password)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isRegistered = true) }
                },
                onFailure = { error ->
                    val authError = error as? AuthRepository.AuthError
                    val fieldErrors = authError?.fieldErrors ?: emptyMap()
                    _uiState.update { it.copy(
                        isLoading = false,
                        generalError = if (fieldErrors.isEmpty()) authError?.message else null,
                        fieldErrors = fieldErrors
                    ) }
                }
            )
        }
    }

    fun clearErrors() {
        _uiState.update { it.copy(generalError = null, fieldErrors = emptyMap()) }
    }

    fun clearFieldError(field: String) {
        _uiState.update { state ->
            val newFieldErrors = state.fieldErrors.toMutableMap()
            newFieldErrors.remove(field)
            state.copy(fieldErrors = newFieldErrors)
        }
    }

    private fun validateFields(
        matricula: String,
        nombre: String,
        email: String,
        password: String
    ): String? {
        if (matricula.isBlank()) return "La matrícula es obligatoria"
        if (nombre.isBlank()) return "El nombre es obligatorio"
        if (email.isBlank()) return "El correo es obligatorio"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "El formato del correo no es válido"
        }
        if (password.isBlank()) return "La contraseña es obligatoria"
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        return null
    }

    class Factory(
        private val application: Application,
        private val authRepository: AuthRepository
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(application, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}