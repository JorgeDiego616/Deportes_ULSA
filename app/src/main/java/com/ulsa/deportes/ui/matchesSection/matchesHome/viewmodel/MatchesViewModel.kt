package com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.domain.repository.SportsRepository
import com.ulsa.deportes.ui.homeSection.homeHome.model.MatchScore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Locale

enum class MatchStatusFilter {
    ALL, LIVE, UPCOMING, FINISHED
}

enum class MatchSortOption {
    RECENT, UPCOMING, SCORE_DIFFERENCE
}

sealed interface MatchesUiState {
    object Loading : MatchesUiState
    data class Success(
        val matches: List<MatchScore>,
        val availableSports: List<String>
    ) : MatchesUiState
    data class Error(val message: String) : MatchesUiState
}

class MatchesViewModel(
    private val repository: SportsRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    private val _allMatches = MutableStateFlow<List<MatchScore>>(emptyList())
    private val _availableSports = MutableStateFlow<List<String>>(emptyList())

    // Filtros y búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSport = MutableStateFlow<String?>(null)
    val selectedSport: StateFlow<String?> = _selectedSport.asStateFlow()

    private val _selectedStatus = MutableStateFlow(MatchStatusFilter.ALL)
    val selectedStatus: StateFlow<MatchStatusFilter> = _selectedStatus.asStateFlow()

    private val _selectedSort = MutableStateFlow(MatchSortOption.RECENT)
    val selectedSort: StateFlow<MatchSortOption> = _selectedSort.asStateFlow()

    // Lista filtrada y ordenada combinada reactivamente
    val filteredMatches: StateFlow<List<MatchScore>> = combine(
        _allMatches,
        _searchQuery,
        _selectedSport,
        _selectedStatus,
        _selectedSort
    ) { matches, query, sport, statusFilter, sortOption ->
        var result = matches

        // 1. Filtro por deporte
        if (!sport.isNullOrBlank()) {
            result = result.filter { it.sport.equals(sport, ignoreCase = true) }
        }

        // 2. Filtro por estado
        if (statusFilter != MatchStatusFilter.ALL) {
            result = result.filter { match ->
                val calculatedStatus = determineMatchStatus(match)
                calculatedStatus == statusFilter
            }
        }

        // 3. Buscador en tiempo real (ignora mayúsculas, minúsculas y acentos)
        if (query.isNotBlank()) {
            val normalizedQuery = query.normalize()
            result = result.filter { match ->
                val homeName = match.homeTeam.name.normalize()
                val homeAbbr = match.homeTeam.abbreviation.normalize()
                val awayName = match.awayTeam.name.normalize()
                val awayAbbr = match.awayTeam.abbreviation.normalize()
                val sportName = match.sport.normalize()
                val summaryText = match.summary.normalize()

                homeName.contains(normalizedQuery) ||
                homeAbbr.contains(normalizedQuery) ||
                awayName.contains(normalizedQuery) ||
                awayAbbr.contains(normalizedQuery) ||
                sportName.contains(normalizedQuery) ||
                summaryText.contains(normalizedQuery)
            }
        }

        // 4. Ordenamiento
        result = when (sortOption) {
            MatchSortOption.RECENT -> result.sortedByDescending { parseInstant(it.playedAt) }
            MatchSortOption.UPCOMING -> result.sortedBy { parseInstant(it.playedAt) }
            MatchSortOption.SCORE_DIFFERENCE -> result.sortedByDescending {
                kotlin.math.abs(it.score.home - it.score.away)
            }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch(defaultDispatcher) {
            _uiState.value = MatchesUiState.Loading
            try {
                val data = repository.getHomeData()
                val matches = data.scores
                _allMatches.value = matches
                val sports = matches.map { it.sport }.distinct().sorted()
                _availableSports.value = sports
                _uiState.value = MatchesUiState.Success(matches, sports)
            } catch (e: Exception) {
                _uiState.value = MatchesUiState.Error(e.message ?: "Error al cargar partidos")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedSport(sport: String?) {
        _selectedSport.value = sport
    }

    fun setSelectedStatus(status: MatchStatusFilter) {
        _selectedStatus.value = status
    }

    fun setSelectedSort(sort: MatchSortOption) {
        _selectedSort.value = sort
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedSport.value = null
        _selectedStatus.value = MatchStatusFilter.ALL
        _selectedSort.value = MatchSortOption.RECENT
    }

    /**
     * Determina el estado del partido basándose en el campo `status` y la fecha `playedAt`.
     */
    fun determineMatchStatus(match: MatchScore): MatchStatusFilter {
        val rawStatus = match.status.normalize()
        if (rawStatus.contains("live") || rawStatus.contains("en vivo") || rawStatus.contains("curso")) {
            return MatchStatusFilter.LIVE
        }
        if (rawStatus.contains("final") || rawStatus.contains("finished") || rawStatus.contains("terminado")) {
            return MatchStatusFilter.FINISHED
        }
        if (rawStatus.contains("proximo") || rawStatus.contains("upcoming") || rawStatus.contains("scheduled")) {
            return MatchStatusFilter.UPCOMING
        }

        // Fallback por fecha si el status textual es ambiguo
        val instant = parseInstant(match.playedAt)
        val now = Instant.now()
        return if (instant.isAfter(now)) {
            MatchStatusFilter.UPCOMING
        } else {
            MatchStatusFilter.FINISHED
        }
    }

    private fun parseInstant(dateStr: String): Instant {
        return try {
            Instant.parse(dateStr)
        } catch (_: DateTimeParseException) {
            try {
                Instant.parse("${dateStr}T00:00:00Z")
            } catch (_: Exception) {
                Instant.EPOCH
            }
        } catch (_: Exception) {
            Instant.EPOCH
        }
    }

    /** Normaliza texto removiendo acentos y pasando a minúsculas. */
    private fun String.normalize(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "").lowercase(Locale.ROOT)
    }

    class Factory(
        private val repository: SportsRepository,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MatchesViewModel::class.java)) {
                return MatchesViewModel(repository, dispatcher) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
