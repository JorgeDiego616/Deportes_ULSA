package com.ulsa.deportes.ui.details.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.domain.repository.SportsRepository
import com.ulsa.deportes.ui.details.util.TeamStats
import com.ulsa.deportes.ui.details.util.calculateTeamStats
import com.ulsa.deportes.ui.homeSection.homeHome.model.MatchScore
import com.ulsa.deportes.ui.homeSection.homeHome.model.NewsItem
import com.ulsa.deportes.ui.homeSection.homeHome.model.SportEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MatchDetailViewModel(
    private val repository: SportsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val matchId: Int = when (val raw = savedStateHandle.get<Any?>("id")) {
        is Int -> raw
        is String -> raw.toIntOrNull() ?: -1
        else -> -1
    }

    private val _match = MutableStateFlow<MatchScore?>(null)
    val match: StateFlow<MatchScore?> = _match.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMatch()
    }

    private fun loadMatch() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getHomeData()
                _match.value = data.scores.find { it.id == matchId }
            } catch (_: Exception) {
                _match.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun provideFactory(repository: SportsRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val handle = extras.createSavedStateHandle()
                return MatchDetailViewModel(repository, handle) as T
            }
        }
    }
}

class NewsDetailViewModel(
    private val repository: SportsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val newsId: Int = when (val raw = savedStateHandle.get<Any?>("id")) {
        is Int -> raw
        is String -> raw.toIntOrNull() ?: -1
        else -> -1
    }

    private val _news = MutableStateFlow<NewsItem?>(null)
    val news: StateFlow<NewsItem?> = _news.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNews()
    }

    private fun loadNews() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getHomeData()
                _news.value = data.news.find { it.id == newsId }
                    ?: data.news.getOrNull(newsId)
                    ?: data.news.firstOrNull()
            } catch (_: Exception) {
                _news.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun provideFactory(repository: SportsRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val handle = extras.createSavedStateHandle()
                return NewsDetailViewModel(repository, handle) as T
            }
        }
    }
}

class EventDetailViewModel(
    private val repository: SportsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val eventId: Int = when (val raw = savedStateHandle.get<Any?>("id")) {
        is Int -> raw
        is String -> raw.toIntOrNull() ?: -1
        else -> -1
    }

    private val _event = MutableStateFlow<SportEvent?>(null)
    val event: StateFlow<SportEvent?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getHomeData()
                _event.value = data.events.find { it.id == eventId }
                    ?: data.events.getOrNull(eventId)
                    ?: data.events.firstOrNull()
            } catch (_: Exception) {
                _event.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun provideFactory(repository: SportsRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val handle = extras.createSavedStateHandle()
                return EventDetailViewModel(repository, handle) as T
            }
        }
    }
}

class TeamDetailViewModel(
    private val repository: SportsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val teamName: String = try {
        val raw = savedStateHandle.get<Any?>("name")?.toString() ?: ""
        URLDecoder.decode(raw, StandardCharsets.UTF_8.toString())
    } catch (_: Exception) {
        savedStateHandle.get<String>("name") ?: ""
    }

    private val _stats = MutableStateFlow<TeamStats?>(null)
    val stats: StateFlow<TeamStats?> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTeamStats()
    }

    private fun loadTeamStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getHomeData()
                _stats.value = calculateTeamStats(teamName, data.scores)
            } catch (_: Exception) {
                _stats.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun provideFactory(repository: SportsRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val handle = extras.createSavedStateHandle()
                return TeamDetailViewModel(repository, handle) as T
            }
        }
    }
}
