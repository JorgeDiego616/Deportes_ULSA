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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.DemoQrForm
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
    var form by remember { mutableStateOf(DemoQrForm.defaults()) }
    var qrCreated by remember { mutableStateOf(false) }
    var createdQrData by remember { mutableStateOf<CreateQrResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val capacity by viewModel.capacity.collectAsState()

    // Opciones para Tipo de evento
    val eventCategories = listOf("Deportivo", "Académico", "Cultural/Artística")
    var categoryExpanded by remember { mutableStateOf(false) }

    // Opciones para Sedes (basadas en infraestructura ULSA)
    val eventVenues = listOf(
        "Estadio La Salle",
        "Gimnasio La Salle",
        "Auditorio ULSA",
        "Plaza Central / Explanada ULSA",
        "Cancha de Fútbol",
        "Cancha de Básquetbol",
        "Salón de Usos Múltiples",
        "Área Cultural"
    )
    var venueExpanded by remember { mutableStateOf(false) }

    // Validación del formulario - todos los campos requeridos
    val isFormValid = remember(form) {
        form.studentName.isNotBlank() &&
        form.studentId.isNotBlank() &&
        form.faculty.isNotBlank() &&
        form.eventTitle.isNotBlank() &&
        form.eventCategory.isNotBlank() &&
        form.eventVenue.isNotBlank()
    }

    // Cargar la capacidad del evento una sola vez al abrir la pantalla.
    LaunchedEffect(form.eventId) {
        viewModel.loadCapacity(form.eventId)
    }

    // Observar éxito en la creación del QR
    LaunchedEffect(state) {
        val currentState = state
        if (currentState is QrUiState.Success) {
            qrCreated = true
            createdQrData = currentState.data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (qrCreated) "Código QR Generado" else "Crear código QR", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (qrCreated) {
                            // Si hay QR creado, volver al formulario
                            qrCreated = false
                            createdQrData = null
                            form = DemoQrForm.defaults()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (qrCreated) "Volver al formulario" else "Regresar"
                        )
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
            if (qrCreated && createdQrData != null) {
                // Botón dividido: 3/4 "Ver QR" + 1/4 icono editar/borrar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Mostrar el QR - navegar a vista de solo QR o mostrar en diálogo
                        },
                        enabled = true,
                        modifier = Modifier
                            .width(0.dp)
                            .weight(0.75f, fill = true)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A73E8),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Visibility, contentDescription = "Ver QR")
                            Spacer(Modifier.width(8.dp))
                            Text("Ver QR", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { showEditDialog = true },
                        enabled = true,
                        modifier = Modifier
                            .width(0.dp)
                            .weight(0.25f, fill = true)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F5),
                            contentColor = Color(0xFF1A73E8)
                        ),
                        shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar o eliminar QR")
                    }
                }
            } else {
                // Botón normal para crear QR
                Button(
                    onClick = { viewModel.createQr(form.toRequest()) },
                    enabled = isFormValid && state !is QrUiState.Loading,
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
                    val currentState = state
                    if (currentState is QrUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Crear código QR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (!qrCreated) {
            // FORMULARIO
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección 1: Datos del Administrador
                SectionCard(title = "Datos del Administrador") {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = form.studentName,
                            onValueChange = { form = form.copy(studentName = it) },
                            label = { Text("Nombre del administrador") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFE6EAF2),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8)
                            )
                        )

                        OutlinedTextField(
                            value = form.studentId,
                            onValueChange = { form = form.copy(studentId = it) },
                            label = { Text("Matrícula / ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFE6EAF2),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8)
                            )
                        )

                        OutlinedTextField(
                            value = form.faculty,
                            onValueChange = { form = form.copy(faculty = it) },
                            label = { Text("Facultad / Departamento") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFE6EAF2),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8)
                            )
                        )
                    }
                }

                // Sección 2: Datos del Evento
                SectionCard(title = "Datos del Evento") {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = form.eventTitle,
                            onValueChange = { form = form.copy(eventTitle = it) },
                            label = { Text("Título del evento") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFE6EAF2),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8)
                            )
                        )

                        // Dropdown Tipo de evento
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = form.eventCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de evento") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1A73E8),
                                    unfocusedBorderColor = Color(0xFFE6EAF2),
                                    focusedLabelColor = Color(0xFF1A73E8),
                                    cursorColor = Color(0xFF1A73E8)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                eventCategories.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            form = form.copy(eventCategory = option)
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Sedes
                        ExposedDropdownMenuBox(
                            expanded = venueExpanded,
                            onExpandedChange = { venueExpanded = !venueExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = form.eventVenue,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sede") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = venueExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1A73E8),
                                    unfocusedBorderColor = Color(0xFFE6EAF2),
                                    focusedLabelColor = Color(0xFF1A73E8),
                                    cursorColor = Color(0xFF1A73E8)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = venueExpanded,
                                onDismissRequest = { venueExpanded = false }
                            ) {
                                eventVenues.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            form = form.copy(eventVenue = option)
                                            venueExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Mensaje de validación
                if (!isFormValid) {
                    Text(
                        "Completa todos los campos para habilitar la creación del QR",
                        textAlign = TextAlign.Center,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                } else {
                    Text(
                        "Todos los campos completos. Presiona el botón para generar tu pase de acceso.",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1F7A4D),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }

                capacity?.let {
                    Text(
                        if (it.isSoldOut) "Cupo agotado"
                        else "Lugares disponibles: ${it.spotsRemaining} / ${it.capacityLimit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (it.isSoldOut) Color(0xFFD32F2F) else Color(0xFF1F7A4D)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        } else {
            // VISTA DE QR CREADO - Mostrar ticket compacto con opción de ver completo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                createdQrData?.let { data ->
                    SectionCard(title = "Tu Pase de Acceso") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // QR Code pequeño
                            val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, data.qr.token) {
                                value = withContext(Dispatchers.Default) { QrGenerator.generate(data.qr.token) }
                            }

                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Código QR",
                                    modifier = Modifier.size(200.dp)
                                )
                            } ?: CircularProgressIndicator(color = Color(0xFF1A73E8))

                            Spacer(Modifier.height(4.dp))

                            Text(
                                data.eventSummary.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                data.eventSummary.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1A73E8),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${data.eventSummary.venue} · ${formatIsoDate(data.eventSummary.startsAt)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4B5563)
                            )

                            data.userSummary?.let {
                                Text(
                                    "${it.studentName} (${it.studentId}) · ${it.faculty}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4B5563),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Text(
                                "Puerta: ${data.accessRules.entryGate}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4B5563)
                            )

                            val status = if (data.accessRules.isExpired()) "Expirado" else statusLabel(data.ticket.status)
                            val statusColor = when {
                                data.accessRules.isExpired() -> Color(0xFFD32F2F)
                                data.ticket.status == TicketStatus.ACTIVE -> Color(0xFF1F7A4D)
                                else -> Color(0xFF6B7280)
                            }
                            Text(
                                "Estado: $status",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }

                Text(
                    "Usa el botón 'Ver QR' para ver el código completo o 'Editar' para modificarlo",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Diálogo para editar o eliminar QR
    if (showEditDialog) {
        Dialog(
            onDismissRequest = { showEditDialog = false },
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
                        .width(280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("¿Qué deseas hacer?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Elige una opción para tu código QR generado.", textAlign = TextAlign.Center)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                qrCreated = false
                                createdQrData = null
                                form = DemoQrForm.defaults()
                                showEditDialog = false
                            },
                            modifier = Modifier.width(0.dp).weight(1f, fill = true),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showEditDialog = false
                                qrCreated = false
                            },
                            modifier = Modifier.width(0.dp).weight(1f, fill = true),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                Spacer(Modifier.width(8.dp))
                                Text("Editar QR", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = { showEditDialog = false },
                            modifier = Modifier.width(0.dp).weight(1f, fill = true),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A73E8))
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(width = 2.dp, color = Color(0xFF1A73E8), shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A73E8),
                modifier = Modifier.fillMaxWidth()
            )
            content()
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
    } ?: CircularProgressIndicator(color = Color(0xFF1A73E8))

    Spacer(Modifier.height(8.dp))

    Text(
        data.eventSummary.title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1A1A),
        textAlign = TextAlign.Center
    )
    Text(
        data.eventSummary.category,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF1A73E8),
        fontWeight = FontWeight.SemiBold
    )
    Text(
        "${data.eventSummary.venue} · ${formatIsoDate(data.eventSummary.startsAt)}",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF4B5563)
    )

    data.userSummary?.let {
        Text(
            "${it.studentName} (${it.studentId}) · ${it.faculty}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4B5563),
            textAlign = TextAlign.Center
        )
    }

    Text(
        "Puerta: ${data.accessRules.entryGate}",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF4B5563)
    )

    val status = if (data.accessRules.isExpired()) "Expirado" else statusLabel(data.ticket.status)
    val statusColor = when {
        data.accessRules.isExpired() -> Color(0xFFD32F2F)
        data.ticket.status == TicketStatus.ACTIVE -> Color(0xFF1F7A4D)
        else -> Color(0xFF6B7280)
    }
    Text(
        "Estado: $status",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = statusColor
    )
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