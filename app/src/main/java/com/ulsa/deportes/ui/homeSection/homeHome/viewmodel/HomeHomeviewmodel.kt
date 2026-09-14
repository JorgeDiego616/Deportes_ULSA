package com.ulsa.deportes.ui.homeSection.homeHome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.ui.homeSection.homeHome.model.HomeData
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomeData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            _uiState.value = try {
                val data = HomeRetrofitClient.service.getHomeData()
                HomeUiState.Success(data)
            } catch (e: IOException) {
                // Sin internet, DNS, timeout, etc. (Render puede tardar en
                // "despertar" si el servicio estaba dormido por inactividad).
                HomeUiState.Error("No se pudo conectar. Verifica tu conexión e intenta de nuevo.")
            } catch (e: HttpException) {
                HomeUiState.Error("Error del servidor (${e.code()}).")
            }
        }
    }
}