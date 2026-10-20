package com.ulsa.deportes.ui.qrSection.qrHome.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.qrSection.qrHome.data.EventPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.data.RegistrationPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.EventResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.RegistrationResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.TicketStatus
import com.ulsa.deportes.ui.qrSection.qrHome.model.getFormattedDate
import com.ulsa.deportes.ui.qrSection.qrHome.model.getFormattedTimeRange
import com.ulsa.deportes.ui.qrSection.qrHome.network.QrDependencies
import com.ulsa.deportes.ui.qrSection.qrHome.util.QrGenerator
import com.ulsa.deportes.ui.qrSection.qrHome.viewmodel.QrUiState
import com.ulsa.deportes.ui.qrSection.qrHome.viewmodel.QrViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreenView(
    event: EventResponse,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val api = remember { QrDependencies.createApi(context) }
    val viewModel: QrViewModel = viewModel(factory = QrViewModel.Factory(api))
    val registrationPreferences = remember { RegistrationPreferences(context) }
    
    var attendeeName by remember { mutableStateOf("") }
    var attendeeEmail by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var registrationSuccess by remember { mutableStateOf(false) }
    var createdRegistration by remember { mutableStateOf<RegistrationResponse?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    // Validación
    val isFormValid = remember(attendeeName, attendeeEmail) {
        attendeeName.isNotBlank() && attendeeEmail.isNotBlank() && attendeeEmail.contains("@")
    }

    // Observar éxito en el registro
    LaunchedEffect(state) {
        val currentState = state
        if (currentState is QrUiState.Success) {
            isRegistering = false
            registrationSuccess = true
            // Convertir CreateQrResponse del mock a RegistrationResponse
            val qrData = currentState.data
            createdRegistration = RegistrationResponse(
                registrationId = java.util.UUID.randomUUID().toString(),
                eventId = event.eventId,
                attendeeName = attendeeName,
                attendeeEmail = attendeeEmail,
                qrToken = qrData.qr.token,
                ticketStatus = TicketStatus.ACTIVE,
                registeredAt = java.time.Instant.now().toString()
            )
            registrationPreferences.saveRegistration(createdRegistration!!)
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrarse a ${event.title}", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A),
                    navigationIconContentColor = Color(0xFF1A73E8)
                )
            )
        },
        bottomBar = {
            if (!registrationSuccess) {
                Button(
                    onClick = { 
                        if (isFormValid && !isRegistering) {
                            isRegistering = true
                            val now = java.time.Instant.now()
                            val request = com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrRequest(
                                eventId = event.eventId,
                                creationMode = com.ulsa.deportes.ui.qrSection.qrHome.model.CreationMode.STATIC_EVENT_CHECKIN,
                                userId = null,
                                validFrom = now.toString(),
                                validUntil = now.plus(1, java.time.temporal.ChronoUnit.DAYS).toString(),
                                securityConfig = com.ulsa.deportes.ui.qrSection.qrHome.model.SecurityConfig(),
                                eventTitle = event.title,
                                eventCategory = event.category.name,
                                eventVenue = event.venue,
                                studentName = attendeeName,
                                studentId = attendeeEmail, // Usamos email como ID temporal
                                faculty = ""
                            )
                            viewModel.createQr(request)
                        }
                    },
                    enabled = isFormValid && !isRegistering,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) Color(0xFF1A73E8) else Color(0xFFE6EAF2),
                        contentColor = if (isFormValid) Color.White else Color(0xFF9FA7B3),
                        disabledContainerColor = Color(0xFFE6EAF2),
                        disabledContentColor = Color(0xFF9FA7B3)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isRegistering) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Registrar")
                            Spacer(Modifier.width(8.dp))
                            Text("Registrarse", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info del evento
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Event, contentDescription = "Evento", tint = Color.White, modifier = Modifier.size(28.dp))
                        Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("${event.getFormattedDate()} · ${event.getFormattedTimeRange()}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    Text(event.venue, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    Text("Cupos: ${event.capacity}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                }
            }

            // Formulario de registro
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Registro de Asistente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Text("Complete sus datos para generar su boleto QR", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280), textAlign = TextAlign.Center)

                    OutlinedTextField(
                        value = attendeeName,
                        onValueChange = { attendeeName = it },
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Nombre", tint = Color(0xFF1A73E8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A73E8),
                            unfocusedBorderColor = Color(0xFFE6EAF2),
                            focusedLabelColor = Color(0xFF1A73E8),
                            cursorColor = Color(0xFF1A73E8)
                        )
                    )

                    OutlinedTextField(
                        value = attendeeEmail,
                        onValueChange = { attendeeEmail = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email", tint = Color(0xFF1A73E8)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A73E8),
                            unfocusedBorderColor = Color(0xFFE6EAF2),
                            focusedLabelColor = Color(0xFF1A73E8),
                            cursorColor = Color(0xFF1A73E8)
                        )
                    )

                    if (!isFormValid && (attendeeName.isNotBlank() || attendeeEmail.isNotBlank())) {
                        Text(
                            "Complete todos los campos correctamente (email debe contener @)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD32F2F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Info del boleto
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.QrCode, contentDescription = "QR", tint = Color(0xFF1A73E8))
                        Spacer(Modifier.width(8.dp))
                        Text("Su boleto QR se generará al registrarse", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A73E8))
                    }
                    Text("Recibirá en su email: nombre del evento, lugar, fecha, hora, cupos y el QR como boleto de entrada", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                }
            }
        }
    }

    // Diálogo de éxito con mock de envío de email
    if (showSuccessDialog) {
        val registration = createdRegistration!!
        Dialog(
            onDismissRequest = { showSuccessDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .wrapContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .width(300.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Éxito", tint = Color(0xFF1F7A4D))
                        Spacer(Modifier.width(8.dp))
                        Text("¡Registrado!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Se ha enviado un email a ${attendeeEmail} con su boleto QR", textAlign = TextAlign.Center)
                        Text("El email incluye:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start) {
                            Text("• Nombre del evento: ${event.title}")
                            Text("• Lugar: ${event.venue}")
                            Text("• Fecha: ${event.getFormattedDate()}")
                            Text("• Hora: ${event.getFormattedTimeRange()}")
                            Text("• Cupos: ${event.capacity}")
                            Text("• QR boleto de entrada (token: ${registration.qrToken.substring(0, 16)}...)")
                        }
                    }

                    // Botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSuccessDialog = false; onBack() },
                            modifier = Modifier.weight(1f, fill = true),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                        ) {
                            Text("Volver al Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showSuccessDialog = false },
                            modifier = Modifier.weight(1f, fill = true),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A73E8))
                        ) {
                            Text("Ver Boleto", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}