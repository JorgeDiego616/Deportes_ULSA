package com.ulsa.deportes.ui.newsSection.newsHome.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.newsSection.newsHome.viewmodel.NewsViewModel
//el que pone formato a las noticias
@Composable
fun NewsHomeView(viewModel: NewsViewModel = viewModel()) {
    val MI_GIST_URL = "https://gist.githubusercontent.com/Rodrigolega/410e6141fb16acee8cd1e71ae6cbf5f8/raw/74308cef8da78c935605bedad5fc7130c5c92aa2/noticias.json"
//LaunchedEffect(Unit) asegura que la descarga se dispare solo una vez cuando la pantalla se muestra por primera vez
    // Disparar la carga de datos al abrir la pantalla
    LaunchedEffect(Unit) {
        if (MI_GIST_URL.contains("usuario")) {
            // Aún no ha puesto su URL real
        } else {
            viewModel.fetchNews(MI_GIST_URL)
        }
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
            viewModel.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            viewModel.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchNews(MI_GIST_URL) }) {
                        Text("Reintentar")
                    }
                }
            }
            MI_GIST_URL.contains("usuario") -> {
                // Mensaje instructivo para el alumno
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("¡Configuración pendiente!", fontWeight = FontWeight.Bold)
                        Text("Para ver tus noticias reales:")
                        Text("1. Crea tu Gist en GitHub con el JSON.")
                        Text("2. Dale click al botón 'Raw'.")
                        Text("3. Pega el link en NewsHomeView.kt.")
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.newsList) { noticia ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = noticia.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = noticia.fecha,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = noticia.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}