package com.ulsa.deportes.ui.homeSection.homeHome.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ulsa.deportes.ui.homeSection.homeHome.model.*
import com.ulsa.deportes.ui.homeSection.homeHome.viewmodel.HomeUiState
import com.ulsa.deportes.ui.homeSection.homeHome.viewmodel.HomeViewModel

@Composable
fun HomeHomeview(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HomeUiState.Error -> {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadHome() }) {
                        Text("Reintentar")
                    }
                }
            }
        }
        is HomeUiState.Success -> {
            HomeContent(state.data)
        }
    }
}

@Composable
private fun HomeContent(homeData: HomeData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        SectionTitle("Resultados")
        Spacer(Modifier.height(8.dp))
        ScoresRow(homeData.scores)

        Spacer(Modifier.height(28.dp))

        SectionTitle("Noticias")
        Spacer(Modifier.height(8.dp))
        NewsSectionList(homeData.news)

        Spacer(Modifier.height(28.dp))

        SectionTitle("Eventos")
        Spacer(Modifier.height(8.dp))
        EventsRow(homeData.events)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ---------- SCORES (horizontal, se desliza a los lados) ----------

@Composable
private fun ScoresRow(scores: List<MatchScore>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(scores) { match -> ScoreCard(match) }
    }
}

@Composable
private fun ScoreCard(match: MatchScore) {
    Card(
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(match.sport, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(match.status, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TeamColumn(match.homeTeam.logo, match.homeTeam.abbreviation)
                Text(
                    "${match.score.home} - ${match.score.away}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TeamColumn(match.awayTeam.logo, match.awayTeam.abbreviation)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                formatIsoDate(match.playedAt),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun TeamColumn(logoUrl: String, abbreviation: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = logoUrl,
            contentDescription = abbreviation,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text(abbreviation, style = MaterialTheme.typography.labelSmall)
    }
}

// ---------- NOTICIAS (arriba hacia abajo, parte del scroll principal) ----------

@Composable
private fun NewsSectionList(news: List<NewsItem>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        news.forEach { item -> NewsCard(item) }
    }
}

@Composable
private fun NewsCard(item: NewsItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.image,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            )
            Column(Modifier.padding(14.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    item.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(item.author, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(formatIsoDate(item.publishedAt), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

// ---------- EVENTOS (horizontal, similar a Scores) ----------

@Composable
private fun EventsRow(events: List<SportEvent>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events) { event -> EventCard(event) }
    }
}

@Composable
private fun EventCard(event: SportEvent) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            AsyncImage(
                model = event.image,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Column(Modifier.padding(12.dp)) {
                Text(event.sport, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(event.venue, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${formatIsoDate(event.startDate)} - ${formatIsoDate(event.endDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ---------- Helper ----------

/** "2026-09-01T20:00:00Z" o "2026-09-01" -> "01/09/2026" (sin usar java.time, para evitar temas de minSdk/desugaring). */
private fun formatIsoDate(iso: String): String {
    val datePart = iso.substringBefore("T")
    val (year, month, day) = datePart.split("-")
    return "$day/$month/$year"
}