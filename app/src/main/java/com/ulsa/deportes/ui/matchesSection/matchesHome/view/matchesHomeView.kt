package com.ulsa.deportes.ui.matchesSection.matchesHome.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ulsa.deportes.data.repository.SportsRepositoryImpl
import com.ulsa.deportes.ui.components.ErrorView
import com.ulsa.deportes.ui.components.LoadingView
import com.ulsa.deportes.ui.components.SportCard
import com.ulsa.deportes.ui.homeSection.homeHome.model.MatchScore
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeRetrofitClient
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.MatchSortOption
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.MatchStatusFilter
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.MatchesUiState
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.MatchesViewModel

@Composable
fun matchesHomeView(
    viewModel: MatchesViewModel = viewModel(
        factory = MatchesViewModel.Factory(SportsRepositoryImpl(HomeRetrofitClient.service))
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredMatches by viewModel.filteredMatches.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSport by viewModel.selectedSport.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Barra de búsqueda y Filtros
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar por equipo, deporte o sede...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            when (val state = uiState) {
                is MatchesUiState.Loading -> {
                    // Mostrar filtros vacíos o deshabilitados mientras carga
                }
                is MatchesUiState.Success -> {
                    // Chips de Deporte
                    val sports = state.availableSports
                    if (sports.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedSport == null,
                                onClick = { viewModel.setSelectedSport(null) },
                                label = { Text("Todos los deportes") }
                            )
                            sports.forEach { sport ->
                                FilterChip(
                                    selected = selectedSport == sport,
                                    onClick = { viewModel.setSelectedSport(if (selectedSport == sport) null else sport) },
                                    label = { Text(sport) }
                                )
                            }
                        }
                    }

                    // Chips de Estado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statuses = listOf(
                            MatchStatusFilter.ALL to "Todos",
                            MatchStatusFilter.LIVE to "En vivo",
                            MatchStatusFilter.UPCOMING to "Próximos",
                            MatchStatusFilter.FINISHED to "Finalizados"
                        )
                        statuses.forEach { (filterEnum, label) ->
                            FilterChip(
                                selected = selectedStatus == filterEnum,
                                onClick = { viewModel.setSelectedStatus(filterEnum) },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Selector de Ordenamiento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredMatches.size} partido(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        var sortExpanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { sortExpanded = true }) {
                                Text(
                                    text = when (selectedSort) {
                                        MatchSortOption.RECENT -> "Orden: Más reciente"
                                        MatchSortOption.UPCOMING -> "Orden: Más próximo"
                                        MatchSortOption.SCORE_DIFFERENCE -> "Orden: Dif. marcador"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Más reciente") },
                                    onClick = {
                                        viewModel.setSelectedSort(MatchSortOption.RECENT)
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Más próximo") },
                                    onClick = {
                                        viewModel.setSelectedSort(MatchSortOption.UPCOMING)
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mayor diferencia de marcador") },
                                    onClick = {
                                        viewModel.setSelectedSort(MatchSortOption.SCORE_DIFFERENCE)
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                is MatchesUiState.Error -> {}
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        // Contenido principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uiState) {
                is MatchesUiState.Loading -> {
                    LoadingView(message = "Cargando partidos deportivos...")
                }
                is MatchesUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadMatches() }
                    )
                }
                is MatchesUiState.Success -> {
                    if (filteredMatches.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No se encontraron partidos con los filtros seleccionados 😢",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.clearFilters() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Limpiar filtros", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredMatches) { match ->
                                MatchCardItem(match = match, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCardItem(match: MatchScore, viewModel: MatchesViewModel) {
    val statusFilter = viewModel.determineMatchStatus(match)
    val statusText = when (statusFilter) {
        MatchStatusFilter.LIVE -> "EN VIVO"
        MatchStatusFilter.UPCOMING -> "PRÓXIMO"
        MatchStatusFilter.FINISHED -> "FINALIZADO"
        else -> match.status.uppercase()
    }
    val statusColor = when (statusFilter) {
        MatchStatusFilter.LIVE -> Color.Red
        MatchStatusFilter.UPCOMING -> MaterialTheme.colorScheme.primary
        MatchStatusFilter.FINISHED -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    SportCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 14.dp
    ) {
        // Cabecera de tarjeta
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = match.sport.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        // Equipos y Marcador
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TeamCol(match.homeTeam.logo, match.homeTeam.abbreviation, match.homeTeam.name)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${match.score.home} - ${match.score.away}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = match.summary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TeamCol(match.awayTeam.logo, match.awayTeam.abbreviation, match.awayTeam.name)
        }

        Spacer(Modifier.height(10.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = match.playedAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TeamCol(logoUrl: String, abbreviation: String, fullName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
        ) {
            AsyncImage(
                model = logoUrl,
                contentDescription = fullName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = abbreviation,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
