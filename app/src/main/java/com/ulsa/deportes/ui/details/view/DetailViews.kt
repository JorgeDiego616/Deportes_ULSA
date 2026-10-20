package com.ulsa.deportes.ui.details.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ulsa.deportes.data.repository.SportsRepositoryImpl
import com.ulsa.deportes.ui.components.ErrorView
import com.ulsa.deportes.ui.components.LoadingView
import com.ulsa.deportes.ui.components.SportCard
import com.ulsa.deportes.ui.components.SportHeader
import com.ulsa.deportes.ui.details.util.formatLongSpanishDate
import com.ulsa.deportes.ui.details.viewmodel.EventDetailViewModel
import com.ulsa.deportes.ui.details.viewmodel.MatchDetailViewModel
import com.ulsa.deportes.ui.details.viewmodel.NewsDetailViewModel
import com.ulsa.deportes.ui.details.viewmodel.TeamDetailViewModel
import com.ulsa.deportes.ui.homeSection.homeHome.model.MatchScore
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeRetrofitClient

// ========================== MATCH DETAIL ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailView(
    onBack: () -> Unit,
    viewModel: MatchDetailViewModel = viewModel(
        factory = MatchDetailViewModel.provideFactory(SportsRepositoryImpl(HomeRetrofitClient.service))
    )
) {
    val match by viewModel.match.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Partido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> LoadingView(message = "Cargando partido...")
                match == null -> ErrorView(message = "Partido no encontrado o datos no disponibles.")
                else -> {
                    val m = match!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Deporte y Estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = m.sport.uppercase(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = m.status.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Equipos y Marcador
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Local
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    AsyncImage(
                                        model = m.homeTeam.logo,
                                        contentDescription = m.homeTeam.name,
                                        modifier = Modifier.fillMaxSize().padding(8.dp).clip(CircleShape),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(m.homeTeam.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(m.homeTeam.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Score
                            Text(
                                text = "${m.score.home} - ${m.score.away}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Visitante
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    AsyncImage(
                                        model = m.awayTeam.logo,
                                        contentDescription = m.awayTeam.name,
                                        modifier = Modifier.fillMaxSize().padding(8.dp).clip(CircleShape),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(m.awayTeam.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(m.awayTeam.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Fecha y Resumen
                        SportCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = formatLongSpanishDate(m.playedAt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Resumen del encuentro",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = m.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}


// ========================== NEWS DETAIL ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailView(
    onBack: () -> Unit,
    viewModel: NewsDetailViewModel = viewModel(
        factory = NewsDetailViewModel.provideFactory(SportsRepositoryImpl(HomeRetrofitClient.service))
    )
) {
    val news by viewModel.news.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Noticia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> LoadingView(message = "Cargando noticia...")
                news == null -> ErrorView(message = "Noticia no encontrada o datos no disponibles.")
                else -> {
                    val n = news!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(n.image.takeIf { !it.isNullOrBlank() })
                                .crossfade(true)
                                .error(com.ulsa.deportes.ui.newsSection.newsHome.util.getNewsPlaceholderRes(n.title, n.shortDescription))
                                .fallback(com.ulsa.deportes.ui.newsSection.newsHome.util.getNewsPlaceholderRes(n.title, n.shortDescription))
                                .placeholder(com.ulsa.deportes.ui.newsSection.newsHome.util.getNewsPlaceholderRes(n.title, n.shortDescription))
                                .build(),
                            contentDescription = n.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = n.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Por ${n.author}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatLongSpanishDate(n.publishedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = n.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 26.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


// ========================== EVENT DETAIL ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailView(
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = viewModel(
        factory = EventDetailViewModel.provideFactory(SportsRepositoryImpl(HomeRetrofitClient.service))
    )
) {
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> LoadingView(message = "Cargando evento...")
                event == null -> ErrorView(message = "Evento no encontrado o datos no disponibles.")
                else -> {
                    val ev = event!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = ev.image,
                            contentDescription = ev.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = ev.sport.uppercase(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = ev.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(text = ev.venue, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${formatLongSpanishDate(ev.startDate)} - ${formatLongSpanishDate(ev.endDate)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Text(
                                text = "Convocatoria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = ev.announcement,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Reglas y Normativa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            ev.rules.forEachIndexed { idx, rule ->
                                Text(
                                    text = "${idx + 1}. $rule",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ========================== TEAM DETAIL ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailView(
    onBack: () -> Unit,
    viewModel: TeamDetailViewModel = viewModel(
        factory = TeamDetailViewModel.provideFactory(SportsRepositoryImpl(HomeRetrofitClient.service))
    )
) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas del Equipo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> LoadingView(message = "Calculando estadísticas...")
                stats == null -> ErrorView(message = "No se pudieron calcular las estadísticas del equipo.")
                else -> {
                    val st = stats!!
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            SportCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Resumen de Rendimiento",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    StatItem(label = "Jugados", value = st.matchesPlayed.toString())
                                    StatItem(label = "Ganados", value = st.wins.toString(), color = Color(0xFF1F7A4D))
                                    StatItem(label = "Empatados", value = st.draws.toString())
                                    StatItem(label = "Perdidos", value = st.losses.toString(), color = Color(0xFFD32F2F))
                                }
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    StatItem(label = "Goles Favor", value = st.goalsFor.toString(), color = MaterialTheme.colorScheme.primary)
                                    StatItem(label = "Goles Contra", value = st.goalsAgainst.toString(), color = MaterialTheme.colorScheme.error)
                                    StatItem(label = "Diferencia", value = (st.goalsFor - st.goalsAgainst).toString())
                                }
                            }
                        }

                        item {
                            SportHeader(title = "Partidos del Equipo (${st.matches.size})")
                        }

                        if (st.matches.isEmpty()) {
                            item {
                                SportCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Este equipo / representante no registra partidos finalizados en la fuente de marcadores actual.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                                    )
                                }
                            }
                        } else {
                            items(st.matches) { match ->
                                TeamMatchItem(match = match)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
        Spacer(Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TeamMatchItem(match: MatchScore) {
    SportCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(match.sport, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(match.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(match.homeTeam.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${match.score.home} - ${match.score.away}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
            Text(match.awayTeam.name, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}
