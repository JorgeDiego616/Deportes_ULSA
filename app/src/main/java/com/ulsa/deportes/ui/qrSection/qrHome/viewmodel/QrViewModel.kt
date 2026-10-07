package com.ulsa.deportes.ui.qrSection.qrHome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.ui.qrSection.qrHome.model.CapacityStatus
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrRequest
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse
import com.ulsa.deportes.ui.qrSection.qrHome.network.QrApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface QrUiState {
    data object Idle : QrUiState
    data object Loading : QrUiState
    data class Success(val data: CreateQrResponse) : QrUiState
    data class Error(val message: String) : QrUiState
}

/**
 * Estado y lógica de la pantalla de QR. Recibe la [QrApi] por constructor
 * (mock o real, según [com.ulsa.deportes.ui.qrSection.qrHome.network.QrDependencies]).
 */
class QrViewModel(private val api: QrApi) : ViewModel() {

    private val _state = MutableStateFlow<QrUiState>(QrUiState.Idle)
    val state: StateFlow<QrUiState> = _state.asStateFlow()

    private val _capacity = MutableStateFlow<CapacityStatus?>(null)
    val capacity: StateFlow<CapacityStatus?> = _capacity.asStateFlow()

    private var refreshJob: Job? = null

    fun createQr(request: CreateQrRequest) {
        viewModelScope.launch {
            _state.value = QrUiState.Loading
            try {
                val response = api.createQr(request)
                _state.value = QrUiState.Success(response)
                scheduleRefresh(request, response)
            } catch (e: IOException) {
                _state.value = QrUiState.Error("No se pudo conectar. Verifica tu conexión e intenta de nuevo.")
            } catch (e: HttpException) {
                _state.value = QrUiState.Error("Error del servidor (${e.code()}).")
            } catch (e: Exception) {
                _state.value = QrUiState.Error(e.message ?: "Error al crear el QR")
            }
        }
    }

    /**
     * Auto-renovación solo para pases dinámicos: si el backend manda refresh_at,
     * espera hasta ese instante y vuelve a pedir el QR. En modo estático
     * (refresh_at = null) no hace nada.
     */
    private fun scheduleRefresh(request: CreateQrRequest, response: CreateQrResponse) {
        refreshJob?.cancel()
        val refreshAt = response.qr.refreshAt ?: return
        refreshJob = viewModelScope.launch {
            val waitMs = refreshAt * 1000 - System.currentTimeMillis()
            if (waitMs > 0) delay(waitMs)
            createQr(request)
        }
    }

    fun loadCapacity(eventId: String) {
        viewModelScope.launch {
            runCatching { api.getCapacity(eventId) }
                .onSuccess { _capacity.value = it }
        }
    }

    override fun onCleared() {
        refreshJob?.cancel()
    }

    class Factory(private val api: QrApi) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QrViewModel::class.java)) {
                return QrViewModel(api) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
