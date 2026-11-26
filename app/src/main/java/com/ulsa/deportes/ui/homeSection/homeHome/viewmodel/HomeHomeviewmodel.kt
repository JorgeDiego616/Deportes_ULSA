package com.ulsa.deportes.ui.homeSection.homeHome.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.R
import com.ulsa.deportes.data.repository.SportsRepositoryImpl
import com.ulsa.deportes.domain.repository.SportsRepository
import com.ulsa.deportes.ui.homeSection.homeHome.model.HomeData
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomeData) : HomeUiState()
    data class Error(@StringRes val messageRes: Int) : HomeUiState()
}

class HomeViewModel(
    private val repository: SportsRepository = SportsRepositoryImpl(HomeRetrofitClient.service)
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    /**
     * Consulta el servidor para obtener los datos reales o de respaldo (fallback) a través del repositorio centralizado.
     */
    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            _uiState.value = try {
                val data = repository.getHomeData()
                HomeUiState.Success(data)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    HomeUiState.Error(R.string.error_server)
                } else {
                    HomeUiState.Error(R.string.error_server)
                }
            } catch (e: java.io.IOException) {
                HomeUiState.Error(R.string.error_no_connection)
            } catch (e: Exception) {
                if (e is NullPointerException || e is ClassCastException || e is IllegalArgumentException || e is IndexOutOfBoundsException) {
                    throw e
                }
                HomeUiState.Error(R.string.common_error)
            }
        }
    }
}
