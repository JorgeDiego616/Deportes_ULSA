package com.ulsa.deportes.ui.newsSection.newsHome.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ulsa.deportes.data.repository.SportsRepositoryImpl
import com.ulsa.deportes.ui.components.SportCard
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeRetrofitClient
import com.ulsa.deportes.ui.newsSection.newsHome.viewmodel.NewsViewModel

@Composable
fun NewsHomeView(
    onNavigateToNews: (Int) -> Unit = {},
    viewModel: NewsViewModel = viewModel(
        factory = NewsViewModel.Factory(SportsRepositoryImpl(HomeRetrofitClient.service))
    )
) {
    val newsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchNews()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Noticias de ULSA Deportes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchNews() }) {
                        Text("Reintentar")
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newsList) { noticia ->
                        SportCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToNews(noticia.id) },
                            contentPadding = 0.dp
                        ) {
                            Column {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data(noticia.image.takeIf { !it.isNullOrBlank() })
                                        .crossfade(true)
                                        .error(com.ulsa.deportes.ui.newsSection.newsHome.util.getNewsPlaceholderRes(noticia.title, noticia.shortDescription))
                                        .fallback(com.ulsa.deportes.ui.newsSection.newsHome.util.getNewsPlaceholderRes(noticia.title, noticia.shortDescription))
                                        .placeholder(com.ulsa.deportes.ui.newsSection.newsHome.util.getNewsPlaceholderRes(noticia.title, noticia.shortDescription))
                                        .build(),
                                    contentDescription = noticia.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                )
                                Column(Modifier.padding(14.dp)) {
                                    Text(
                                        text = noticia.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = noticia.shortDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            noticia.author,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            formatIsoDate(noticia.publishedAt),
                                            style = MaterialTheme.typography.labelSmall,
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
    }
}

private fun formatIsoDate(iso: String): String {
    return try {
        val datePart = iso.substringBefore("T")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else iso
    } catch (_: Exception) {
        iso
    }
}
