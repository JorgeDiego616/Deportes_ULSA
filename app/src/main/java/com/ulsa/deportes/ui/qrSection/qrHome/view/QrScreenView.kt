package com.ulsa.deportes.ui.qrSection.qrHome.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.qrSection.qrHome.model.AccessRules
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.DemoQrEvent
import com.ulsa.deportes.ui.qrSection.qrHome.model.TicketStatus
import com.ulsa.deportes.ui.qrSection.qrHome.model.isExpired
import com.ulsa.deportes.ui.qrSection.qrHome.network.QrDependencies
import com.ulsa.deportes.ui.qrSection.qrHome.util.QrGenerator
import com.ulsa.deportes.ui.qrSection.qrHome.viewmodel.QrUiState
import com.ulsa.deportes.ui.qrSection.qrHome.viewmodel.QrViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreenView(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val api = remember { QrDependencies.createApi(context) }
    val viewModel: QrViewModel = viewModel(factory = QrViewModel.Factory(api))
    val request = remember { DemoQrEvent.request() }

    val state by viewModel.state.collectAsState()
    val capacity by viewModel.capacity.collectAsState()

    // Cargar la capacidad del evento una sola vez al abrir la pantalla.
    androidx.compose.runtime.LaunchedEffect(request.eventId) {
        viewModel.loadCapacity(request.eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear código QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.createQr(request) },
                enabled = state !is QrUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Crear código QR", fontWeight = FontWeight.Bold)
            }

            when (val s = state) {
                QrUiState.Idle -> Text(
                    "Presiona el botón para generar tu pase de acceso.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                QrUiState.Loading -> CircularProgressIndicator()

                is QrUiState.Error -> Text(
                    s.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                is QrUiState.Success -> TicketContent(s.data)
            }

            capacity?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    if (it.isSoldOut) "Cupo agotado"
                    else "Lugares disponibles: ${it.spotsRemaining} / ${it.capacityLimit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TicketContent(data: CreateQrResponse) {
    // Generar el bitmap solo cuando cambia el token, fuera del hilo principal.
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, data.qr.token) {
        value = withContext(Dispatchers.Default) { QrGenerator.generate(data.qr.token) }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Código QR",
            modifier = Modifier.size(260.dp)
        )
    } ?: CircularProgressIndicator()

    Spacer(Modifier.height(4.dp))

    Text(data.eventSummary.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "${data.eventSummary.venue} · ${formatIsoDate(data.eventSummary.startsAt)}",
        style = MaterialTheme.typography.bodyMedium
    )

    data.userSummary?.let {
        Text("${it.studentName} (${it.studentId}) · ${it.faculty}", style = MaterialTheme.typography.bodyMedium)
    }

    Text("Puerta: ${data.accessRules.entryGate}", style = MaterialTheme.typography.bodyMedium)

    val status = if (data.accessRules.isExpired()) "Expirado" else statusLabel(data.ticket.status)
    Text("Estado: $status", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
}

private fun statusLabel(status: TicketStatus): String = when (status) {
    TicketStatus.ACTIVE -> "Activo"
    TicketStatus.USED -> "Usado"
    TicketStatus.CANCELLED -> "Cancelado"
}

/** "2026-09-01T20:00:00Z" -> "01/09/2026". */
private fun formatIsoDate(iso: String): String {
    val datePart = iso.substringBefore("T")
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else datePart
}
