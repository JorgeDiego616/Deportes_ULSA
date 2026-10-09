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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Visibility

import androidx.compose.material3.AlertDialog
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
import com.ulsa.deportes.ui.qrSection.qrHome.data.EventPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.data.QrPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.DemoQrForm
import com.ulsa.deportes.ui.qrSection.qrHome.model.EventCategory
import com.ulsa.deportes.ui.qrSection.qrHome.model.EventFormData
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
    val qrPreferences = remember { QrPreferences(context) }
    val eventPreferences = remember { EventPreferences(context) }
    
    // Cargar lista de eventos existentes al inicio
    var existingEventList by remember { mutableStateOf<List<CreateQrResponse>>(qrPreferences.getQrList()) }
    var form by remember { mutableStateOf(EventFormData(
        organizerName = "Jorge Chaparro",
        organizerId = "14446",
        organizerFaculty = "Ingeniería",
        title = "Clásico ULSA 2026",
        category = EventCategory.DEPORTIVO,
        venue = "Estadio La Salle",
        date = java.time.LocalDate.now().plusDays(7),
        startTime = java.time.LocalTime.of(18, 0),
        endTime = java.time.LocalTime.of(20, 0),
        capacity = 100
    )) }
    var qrCreated by remember { mutableStateOf(false) }
    var createdQrData by remember { mutableStateOf<CreateQrResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showExistingQrDialog by remember { mutableStateOf(false) }
    var selectedQrForView by remember { mutableStateOf<CreateQrResponse?>(null) }

    val state by viewModel.state.collectAsState()
    val capacity by viewModel.capacity.collectAsState()

    // Helpers
    val hasAnyQr = existingEventList.isNotEmpty()
    val isAtMaxLimit = existingEventList.size >= 3
    val latestQr = existingEventList.lastOrNull()

    // Opciones para Tipo de evento (usando enum)
    val eventCategories = EventCategory.values()
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
        form.organizerName.isNotBlank() &&
        form.organizerId.isNotBlank() &&
        form.organizerFaculty.isNotBlank() &&
        form.title.isNotBlank() &&
        form.category != null &&
        form.venue.isNotBlank() &&
        form.capacity > 0
    }

    // Observar éxito en la creación del evento
    LaunchedEffect(state) {
        val currentState = state
        if (currentState is QrUiState.Success) {
            qrCreated = true
            createdQrData = currentState.data
        }
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
                title = { 
                    Text(
                        when {
                            qrCreated -> "Evento Creado"
                            selectedQrForView != null -> "Ver Evento"
                            hasAnyQr -> "Tus Eventos (${existingEventList.size}/3)"
                            else -> "Crear Evento"
                        }, 
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (qrCreated) {
                            qrCreated = false
                            createdQrData = null
                        } else if (selectedQrForView != null) {
                            selectedQrForView = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = when {
                                qrCreated -> "Volver al formulario"
                                selectedQrForView != null -> "Volver a la lista"
                                else -> "Regresar"
                            }
                        )
                    }
                },
                actions = {
                    if (qrCreated || hasAnyQr || selectedQrForView != null) {
                        androidx.compose.material3.IconButton(onClick = { onBack() }) {
                            Icon(Icons.Filled.Home, contentDescription = "Volver al perfil")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A),
                    navigationIconContentColor = Color(0xFF1A73E8),
                    actionIconContentColor = Color(0xFF1A73E8)
                )
            )
        },
        bottomBar = {
            when {
                qrCreated && createdQrData != null -> {
                    Button(
                        onClick = {
                            qrPreferences.saveQr(createdQrData!!)
                            existingEventList = qrPreferences.getQrList()
                            qrCreated = false
                            createdQrData = null
                            onBack()
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A73E8),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Event, contentDescription = "Evento")
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar Evento", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                hasAnyQr && !qrCreated && selectedQrForView == null -> {
                    if (!isAtMaxLimit) {
                        Button(
                            onClick = { },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A73E8),
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Event, contentDescription = "Nuevo Evento")
                                Spacer(Modifier.width(8.dp))
                                Text("Crear otro Evento (${existingEventList.size}/3)", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE6EAF2),
                                contentColor = Color(0xFF9FA7B3)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Límite alcanzado (3/3 Eventos)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = { 
                            if (isFormValid && state !is QrUiState.Loading) {
                                // Convertir EventFormData a CreateQrRequest para el mock
                                val now = java.time.Instant.now()
                                val request = com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrRequest(
                                    eventId = form.eventId,
                                    creationMode = com.ulsa.deportes.ui.qrSection.qrHome.model.CreationMode.STATIC_EVENT_CHECKIN,
                                    userId = null,
                                    validFrom = now.toString(),
                                    validUntil = now.plus(30, java.time.temporal.ChronoUnit.DAYS).toString(),
                                    securityConfig = com.ulsa.deportes.ui.qrSection.qrHome.model.SecurityConfig(),
                                    eventTitle = form.title,
                                    eventCategory = form.category.name,
                                    eventVenue = form.venue,
                                    studentName = form.organizerName,
                                    studentId = form.organizerId,
                                    faculty = form.organizerFaculty
                                )
                                viewModel.createQr(request)
                            }
                        },
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
                            Text("Generar Evento", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        // Mostrar formulario solo si NO hay QR creado Y NO hay QR seleccionado Y NO hay lista
        if (!qrCreated && selectedQrForView == null && !hasAnyQr) {
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
                SectionCard(title = "Datos del Organizador") {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = form.organizerName,
                            onValueChange = { form = form.copy(organizerName = it) },
                            label = { Text("Nombre del organizador") },
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
                            value = form.organizerId,
                            onValueChange = { form = form.copy(organizerId = it) },
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
                            value = form.organizerFaculty,
                            onValueChange = { form = form.copy(organizerFaculty = it) },
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
                            value = form.title,
                            onValueChange = { form = form.copy(title = it) },
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

                        // Dropdown Tipo de evento (enum)
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = form.category?.name ?: "",
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
                                        text = { Text(option.name) },
                                        onClick = {
                                            form = form.copy(category = option)
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
                                value = form.venue,
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
                                            form = form.copy(venue = option)
                                            venueExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Fecha del evento (campo de texto con formato dd/MM/yyyy)
                        OutlinedTextField(
                            value = form.date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            onValueChange = { newValue ->
                                // Intentar parsear la fecha si tiene formato válido
                                try {
                                    val parsed = java.time.LocalDate.parse(newValue, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    form = form.copy(date = parsed)
                                } catch (e: Exception) {
                                    // Ignorar formato inválido temporalmente
                                }
                            },
                            label = { Text("Fecha del evento (dd/MM/yyyy)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFE6EAF2),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8)
                            )
                        )

                        // Hora de inicio (campo de texto HH:mm)
                        OutlinedTextField(
                            value = form.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                            onValueChange = { newValue ->
                                try {
                                    val parsed = java.time.LocalTime.parse(newValue, java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                    form = form.copy(startTime = parsed)
                                } catch (e: Exception) {
                                    // Ignorar formato inválido
                                }
                            },
                            label = { Text("Hora de inicio (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFE6EAF2),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8)
                            )
                        )

                        // Hora de fin (campo de texto HH:mm)
                        OutlinedTextField(
                            value = form.endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                            onValueChange = { newValue ->
                                try {
                                    val parsed = java.time.LocalTime.parse(newValue, java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                    form = form.copy(endTime = parsed)
                                } catch (e: Exception) {
                                    // Ignorar formato inválido
                                }
                            },
                            label = { Text("Hora de fin (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFE6EAF2),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8)
                            )
                        )

                        // Capacidad
                        OutlinedTextField(
                            value = form.capacity.toString(),
                            onValueChange = { form = form.copy(capacity = it.toIntOrNull() ?: 100) },
                            label = { Text("Capacidad (cupos)") },
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
            // VISTA DE LISTA DE QRS O QR SELECCIONADO
            if (selectedQrForView != null) {
                // Ver QR individual seleccionado
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val data = selectedQrForView!!
                    SectionCard(title = "Evento") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, data.qr.token) {
                                value = withContext(Dispatchers.Default) { QrGenerator.generate(data.qr.token) }
                            }

                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "QR del Evento",
                                    modifier = Modifier.size(240.dp)
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

                    Spacer(Modifier.height(16.dp))
                }
            } else if (hasAnyQr) {
                // Lista de Eventos guardados
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Tienes ${existingEventList.size} evento(s) guardado(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )

                    existingEventList.reversed().forEachIndexed { index, qr ->
                        val reversedIndex = existingEventList.size - index
                        SectionCard(title = "Evento #$reversedIndex - ${qr.eventSummary.title}") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            qr.eventSummary.category,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF1A73E8),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${qr.eventSummary.venue} · ${formatIsoDate(qr.eventSummary.startsAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6B7280)
                                        )
                                        Text(
                                            "Puerta: ${qr.accessRules.entryGate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { selectedQrForView = qr },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF1A73E8),
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text("Ver", fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                qrPreferences.removeQr(qr.ticketId)
                                                existingEventList = qrPreferences.getQrList()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFD32F2F),
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text("Eliminar", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            } else if (qrCreated) {
                // Evento recién creado (antes de guardar)
                val data = createdQrData!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionCard(title = "Tu Evento") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, data.qr.token) {
                                value = withContext(Dispatchers.Default) { QrGenerator.generate(data.qr.token) }
                            }

                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "QR del Evento",
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

                    Text(
                        "Presiona 'Guardar Evento' para guardarlo y volver al perfil.",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Diálogo cuando ya existe QR e intenta crear uno nuevo (límite 3)
    if (showExistingQrDialog) {
        AlertDialog(
            onDismissRequest = { showExistingQrDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        qrPreferences.clearAll()
                        existingEventList = qrPreferences.getQrList()
                        showExistingQrDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar todos", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExistingQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A73E8))
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Límite de QRs alcanzado", fontWeight = FontWeight.Bold) },
            text = {
                Text("Ya tienes 3 códigos QR guardados (máximo permitido). ¿Deseas eliminar todos para crear uno nuevo?")
            }
        )
    }

    // Diálogo para editar o eliminar QR (para QR recién creado antes de guardar)
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

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                qrCreated = false
                                createdQrData = null
                                showEditDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Eliminar QR", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showEditDialog = false
                                qrCreated = false
                            },
                            modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.fillMaxWidth(),
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
            contentDescription = "QR del Evento",
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