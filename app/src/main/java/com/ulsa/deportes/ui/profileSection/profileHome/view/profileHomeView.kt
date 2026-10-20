package com.ulsa.deportes.ui.profileSection.profileHome.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.profileSection.profileHome.viewmodel.profileHomeViewModel
import com.ulsa.deportes.ui.qrSection.qrHome.data.EventPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.data.QrPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.EventResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.getFormattedDate
import com.ulsa.deportes.ui.qrSection.qrHome.model.getFormattedTimeRange

@Composable
fun profileHomeView(
    onLogout: () -> Unit = {},
    onNavigateToQr: () -> Unit = {},
    onRegisterToEvent: (EventResponse) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") homeViewModel: profileHomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val eventPreferences = remember { EventPreferences(context) }
    val qrPreferences = remember { QrPreferences(context) }
    val eventListState = remember { mutableStateOf(eventPreferences.getEventList()) }
    val eventList by eventListState

    var eventToDelete by remember { mutableStateOf<EventResponse?>(null) }

    // Refrescar la lista de eventos cuando la pantalla se hace visible (RESUMED)
    LaunchedEffect(lifecycleOwner.lifecycle.currentState) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            eventListState.value = eventPreferences.getEventList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Información Personal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar eventos guardados con botón para abrir y botón para eliminar
        if (eventList.isNotEmpty()) {
            Text(
                "Tus eventos (${eventList.size}/3)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                eventList.reversed().forEach { event ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón del evento (abre registro/detalle)
                        Button(
                            onClick = { onRegisterToEvent(event) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A73E8),
                                contentColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f, fill = true),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        event.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${event.getFormattedDate()} · ${event.getFormattedTimeRange()} · ${event.venue}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ArrowRight,
                                    contentDescription = "Registrarse",
                                    tint = Color.White
                                )
                            }
                        }

                        // Botón de eliminar con icono de bote de basura
                        IconButton(
                            onClick = { eventToDelete = event },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar evento",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onNavigateToQr,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A73E8),
                contentColor = Color.White
            )
        ) {
            Icon(imageVector = Icons.Filled.Event, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Evento", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }

    // Diálogo de confirmación para eliminar evento utilizando identificadores únicos (eventId y qrToken)
    if (eventToDelete != null) {
        val target = eventToDelete!!
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("¿Eliminar evento?", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Estás seguro de que deseas eliminar el evento '${target.title}'? También se eliminará su código QR asociado.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Sincronización precisa por identificadores únicos (eventId y qrToken) sin afectar otros eventos con títulos similares
                        eventPreferences.removeEvent(target.eventId)
                        eventPreferences.removeEvent(target.qrToken)

                        qrPreferences.removeQr(target.eventId)
                        qrPreferences.removeQr(target.qrToken)

                        eventListState.value = eventPreferences.getEventList()
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { eventToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A73E8))
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
