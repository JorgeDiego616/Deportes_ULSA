package com.ulsa.deportes.ui.qrSection.qrHome.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.R
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
        organizerName = eventPreferences.getOrganizerName(),
        organizerId = eventPreferences.getOrganizerId(),
        organizerFaculty = eventPreferences.getOrganizerFaculty(),
        title = context.getString(R.string.qr_default_event_title),
        category = EventCategory.DEPORTIVO,
        venue = context.getString(R.string.qr_default_venue),
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
    var editingEvent by remember { mutableStateOf<CreateQrResponse?>(null) }
    var itemToDelete by remember { mutableStateOf<CreateQrResponse?>(null) }
    var showForm by remember { mutableStateOf(false) }

    // Text states for date/time/capacity fields to allow intermediate input
    var dateText by remember { mutableStateOf(form.date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
    var startTimeText by remember { mutableStateOf(form.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))) }
    var endTimeText by remember { mutableStateOf(form.endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))) }
    var capacityText by remember { mutableStateOf(form.capacity.toString()) }

    val state by viewModel.state.collectAsState()
    val capacity by viewModel.capacity.collectAsState()

    // Helpers
    val hasAnyQr = existingEventList.isNotEmpty()
    val isAtMaxLimit = existingEventList.size >= 3
    val latestQr = existingEventList.lastOrNull()

    // Inicializar showForm: true si no hay eventos, false si ya hay eventos
    val showFormInitialized = remember { mutableStateOf(false) }
    if (!showFormInitialized.value) {
        showForm = !hasAnyQr
        showFormInitialized.value = true
    }

    // Opciones para Tipo de evento (usando enum)
    val eventCategories = EventCategory.values()
    var categoryExpanded by remember { mutableStateOf(false) }

    // Opciones para Sedes (basadas en infraestructura ULSA)
    val eventVenues = stringArrayResource(R.array.qr_venues).toList()
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

    // Observar éxito en la generación o actualización del QR/evento
    LaunchedEffect(state) {
        val currentState = state
        if (currentState is QrUiState.Success) {
            if (editingEvent != null) {
                val targetId = editingEvent!!.ticketId
                val eventsBefore = eventPreferences.getEventList()
                val qrsBefore = qrPreferences.getQrList()

                val eventResponse = com.ulsa.deportes.ui.qrSection.qrHome.model.EventResponse(
                    eventId = targetId,
                    title = form.title,
                    category = form.category,
                    venue = form.venue,
                    date = form.date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                    startTime = form.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = form.endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                    organizerName = form.organizerName,
                    organizerId = form.organizerId,
                    organizerFaculty = form.organizerFaculty,
                    capacity = form.capacity,
                    qrToken = currentState.data.qr.token,
                    createdAt = java.time.Instant.now().toString()
                )

                eventPreferences.saveOrganizerDefaults(form.organizerName, form.organizerId, form.organizerFaculty)
                val savedEvent = eventPreferences.saveEvent(eventResponse)
                val savedQr = qrPreferences.saveQr(currentState.data)

                if (savedEvent && savedQr) {
                    existingEventList = qrPreferences.getQrList()
                    editingEvent = null
                    qrCreated = false
                    createdQrData = null
                    showForm = false
                    onBack()
                } else {
                    eventPreferences.restoreList(eventsBefore)
                    qrPreferences.restoreList(qrsBefore)
                    android.widget.Toast.makeText(context, context.getString(R.string.qr_toast_update_error), android.widget.Toast.LENGTH_LONG).show()
                }
            } else {
                qrCreated = true
                createdQrData = currentState.data
                showForm = true
            }
        }
    }

    // Cargar datos del evento en el formulario cuando se edita
    LaunchedEffect(editingEvent) {
        editingEvent?.let { qr ->
            val eventSummary = qr.eventSummary
            val userSummary = qr.userSummary
            val accessRules = qr.accessRules
            
            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val isoDateFormatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
            
            val parsedDate = try {
                java.time.LocalDate.parse(eventSummary.startsAt, isoDateFormatter)
            } catch (e: Exception) {
                java.time.LocalDate.now().plusDays(7)
            }
            
            val parsedStartTime = try {
                java.time.LocalTime.parse(accessRules.validFrom, timeFormatter)
            } catch (e: Exception) {
                java.time.LocalTime.of(18, 0)
            }
            
            val parsedEndTime = try {
                java.time.LocalTime.parse(accessRules.validUntil, timeFormatter)
            } catch (e: Exception) {
                java.time.LocalTime.of(20, 0)
            }
            
            val category = EventCategory.values().find { it.name == eventSummary.category } ?: EventCategory.DEPORTIVO
            
            form = EventFormData(
                organizerName = userSummary?.studentName ?: context.getString(R.string.qr_default_organizer_name),
                organizerId = userSummary?.studentId ?: context.getString(R.string.qr_default_organizer_id),
                organizerFaculty = userSummary?.faculty ?: context.getString(R.string.qr_default_faculty),
                title = eventSummary.title,
                category = category,
                venue = eventSummary.venue,
                date = parsedDate,
                startTime = parsedStartTime,
                endTime = parsedEndTime,
                capacity = 100,
                eventId = qr.ticketId
            )
            
            dateText = parsedDate.format(dateFormatter)
            startTimeText = parsedStartTime.format(timeFormatter)
            endTimeText = parsedEndTime.format(timeFormatter)
            capacityText = "100"
            
            qrCreated = true
            createdQrData = qr
            showForm = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when {
                            qrCreated && editingEvent != null -> stringResource(R.string.qr_title_edit)
                            qrCreated -> stringResource(R.string.qr_title_created)
                            selectedQrForView != null -> stringResource(R.string.qr_title_view)
                            hasAnyQr -> stringResource(R.string.qr_title_your_events, existingEventList.size)
                            else -> stringResource(R.string.qr_title_create)
                        }, 
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (qrCreated && editingEvent != null) {
                            editingEvent = null
                            qrCreated = false
                            createdQrData = null
                            showForm = false
                        } else if (qrCreated) {
                            qrCreated = false
                            createdQrData = null
                            showForm = false
                        } else if (selectedQrForView != null) {
                            selectedQrForView = null
                            showForm = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = when {
                                qrCreated && editingEvent != null -> stringResource(R.string.qr_cd_cancel_edit)
                                qrCreated -> stringResource(R.string.qr_cd_back_to_form)
                                selectedQrForView != null -> stringResource(R.string.qr_cd_back_to_list)
                                else -> stringResource(R.string.qr_cd_back)
                            }
                        )
                    }
                },
                actions = {
                    if (qrCreated || hasAnyQr || selectedQrForView != null) {
                        androidx.compose.material3.IconButton(onClick = { onBack() }) {
                            Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.qr_cd_back_to_profile))
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
                            val targetId = createdQrData!!.ticketId
                            // Verificar si se puede guardar en ambas persistencias antes de proceder
                            if (!eventPreferences.canSave(targetId) || !qrPreferences.canSave(targetId)) {
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.qr_toast_limit_reached),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            // Tomar respaldo previo para restaurar exactamente en caso de fallo (Rollback seguro)
                            val eventsBefore = eventPreferences.getEventList()
                            val qrsBefore = qrPreferences.getQrList()

                            val eventResponse = com.ulsa.deportes.ui.qrSection.qrHome.model.EventResponse(
                                eventId = targetId,
                                title = form.title,
                                category = form.category,
                                venue = form.venue,
                                date = form.date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                                startTime = form.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                                endTime = form.endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                                organizerName = form.organizerName,
                                organizerId = form.organizerId,
                                organizerFaculty = form.organizerFaculty,
                                capacity = form.capacity,
                                qrToken = createdQrData!!.qr.token,
                                createdAt = java.time.Instant.now().toString()
                            )

                            // Guardar preferencias del organizador ingresado
                            eventPreferences.saveOrganizerDefaults(form.organizerName, form.organizerId, form.organizerFaculty)

                            // Guardado atómico
                            val savedEvent = eventPreferences.saveEvent(eventResponse)
                            val savedQr = qrPreferences.saveQr(createdQrData!!)

                            if (savedEvent && savedQr) {
                                existingEventList = qrPreferences.getQrList()
                                qrCreated = false
                                createdQrData = null
                                editingEvent = null
                                showForm = false
                                onBack()
                            } else {
                                // Rollback al estado exacto previo sin alterar registros anteriores
                                eventPreferences.restoreList(eventsBefore)
                                qrPreferences.restoreList(qrsBefore)
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.qr_toast_save_error),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
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
                            Icon(Icons.Filled.Event, contentDescription = stringResource(R.string.qr_cd_event))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (editingEvent != null) stringResource(R.string.qr_update_event)
                                else stringResource(R.string.qr_save_event),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                hasAnyQr && !qrCreated && selectedQrForView == null && !showForm -> {
                    if (!isAtMaxLimit) {
                        Button(
                            onClick = {
                                form = EventFormData(
                                    organizerName = eventPreferences.getOrganizerName(form.organizerName),
                                    organizerId = eventPreferences.getOrganizerId(form.organizerId),
                                    organizerFaculty = eventPreferences.getOrganizerFaculty(form.organizerFaculty),
                                    title = context.getString(R.string.qr_default_event_title),
                                    category = EventCategory.DEPORTIVO,
                                    venue = context.getString(R.string.qr_default_venue),
                                    date = java.time.LocalDate.now().plusDays(7),
                                    startTime = java.time.LocalTime.of(18, 0),
                                    endTime = java.time.LocalTime.of(20, 0),
                                    capacity = 100
                                )
                                dateText = form.date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                startTimeText = form.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                endTimeText = form.endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                capacityText = form.capacity.toString()
                                showForm = true
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
                                Icon(Icons.Filled.Event, contentDescription = stringResource(R.string.qr_cd_new_event))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.qr_create_another_event, existingEventList.size), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { showExistingQrDialog = true },
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
                            Text(stringResource(R.string.qr_limit_reached_delete), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = { 
                            if (eventPreferences.isAtMaxLimit() || qrPreferences.isAtMaxLimit() || existingEventList.size >= 3) {
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.qr_toast_limit_reached_create),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } else if (isFormValid && state !is QrUiState.Loading) {
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
                        enabled = isFormValid && state !is QrUiState.Loading && !isAtMaxLimit,
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
                            Text(
                                if (editingEvent != null) stringResource(R.string.qr_update_event)
                                else stringResource(R.string.qr_generate_event),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        // Mostrar formulario cuando: no hay QR creado, no hay QR seleccionado, y se quiere mostrar formulario
        if (!qrCreated && selectedQrForView == null && showForm) {
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
                SectionCard(title = stringResource(R.string.qr_section_organizer)) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = form.organizerName,
                            onValueChange = { form = form.copy(organizerName = it) },
                            label = { Text(stringResource(R.string.qr_organizer_name)) },
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
                            label = { Text(stringResource(R.string.qr_organizer_id)) },
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
                            label = { Text(stringResource(R.string.qr_organizer_faculty)) },
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
                SectionCard(title = stringResource(R.string.qr_section_event)) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = form.title,
                            onValueChange = { form = form.copy(title = it) },
                            label = { Text(stringResource(R.string.qr_event_title)) },
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
                            onExpandedChange = { categoryExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoryExpanded = !categoryExpanded }
                            ) {
                                OutlinedTextField(
                                    value = form.category?.displayName() ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text(stringResource(R.string.qr_event_type)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color(0xFF1A73E8),
                                        disabledLabelColor = Color(0xFF1A73E8),
                                        disabledTextColor = Color(0xFF1A1A1A),
                                        disabledTrailingIconColor = Color(0xFF1A73E8)
                                    )
                                )
                            }
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                eventCategories.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.displayName()) },
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
                            onExpandedChange = { venueExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { venueExpanded = !venueExpanded }
                            ) {
                                OutlinedTextField(
                                    value = form.venue,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text(stringResource(R.string.qr_venue)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = venueExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color(0xFF1A73E8),
                                        disabledLabelColor = Color(0xFF1A73E8),
                                        disabledTextColor = Color(0xFF1A1A1A),
                                        disabledTrailingIconColor = Color(0xFF1A73E8)
                                    )
                                )
                            }
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
                            value = dateText,
                            onValueChange = { newValue ->
                                // Auto-formato: insertar '/' automáticamente
                                val digits = newValue.filter { it.isDigit() }
                                val formatted = buildString {
                                    for (i in digits.indices) {
                                        if (i == 2 || i == 4) append('/')
                                        append(digits[i])
                                    }
                                }
                                dateText = formatted.take(10) // dd/MM/yyyy = 10 chars
                                // Intentar parsear la fecha si tiene formato válido completo
                                if (dateText.length == 10) {
                                    try {
                                        val parsed = java.time.LocalDate.parse(dateText, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        form = form.copy(date = parsed)
                                    } catch (e: Exception) {
                                        // Ignorar formato inválido temporalmente
                                    }
                                }
                            },
                            label = { Text(stringResource(R.string.qr_event_date)) },
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
                            value = startTimeText,
                            onValueChange = { newValue ->
                                // Auto-formato: insertar ':' automáticamente
                                val digits = newValue.filter { it.isDigit() }
                                val formatted = buildString {
                                    for (i in digits.indices) {
                                        if (i == 2) append(':')
                                        append(digits[i])
                                    }
                                }
                                startTimeText = formatted.take(5) // HH:mm = 5 chars
                                // Intentar parsear la hora si tiene formato válido completo
                                if (startTimeText.length == 5) {
                                    try {
                                        val parsed = java.time.LocalTime.parse(startTimeText, java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                        form = form.copy(startTime = parsed)
                                    } catch (e: Exception) {
                                        // Ignorar formato inválido, permitir edición intermedia
                                    }
                                }
                            },
                            label = { Text(stringResource(R.string.qr_start_time)) },
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
                            value = endTimeText,
                            onValueChange = { newValue ->
                                // Auto-formato: insertar ':' automáticamente
                                val digits = newValue.filter { it.isDigit() }
                                val formatted = buildString {
                                    for (i in digits.indices) {
                                        if (i == 2) append(':')
                                        append(digits[i])
                                    }
                                }
                                endTimeText = formatted.take(5) // HH:mm = 5 chars
                                // Intentar parsear la hora si tiene formato válido completo
                                if (endTimeText.length == 5) {
                                    try {
                                        val parsed = java.time.LocalTime.parse(endTimeText, java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                        form = form.copy(endTime = parsed)
                                    } catch (e: Exception) {
                                        // Ignorar formato inválido, permitir edición intermedia
                                    }
                                }
                            },
                            label = { Text(stringResource(R.string.qr_end_time)) },
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
                            value = capacityText,
                            onValueChange = { newValue ->
                                capacityText = newValue
                                // Solo actualizar form si el valor es un número válido > 0
                                // Si está vacío o es 0, no actualizar (dejar el valor previo en form)
                                val parsed = newValue.toIntOrNull()
                                if (parsed != null && parsed > 0) {
                                    form = form.copy(capacity = parsed)
                                }
                            },
                            label = { Text(stringResource(R.string.qr_capacity)) },
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
                        stringResource(R.string.qr_validation_incomplete),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                } else {
                    Text(
                        stringResource(R.string.qr_validation_complete),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1F7A4D),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }

                capacity?.let {
                    Text(
                        if (it.isSoldOut) stringResource(R.string.qr_sold_out)
                        else stringResource(R.string.qr_spots_available, it.spotsRemaining, it.capacityLimit),
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
                    SectionCard(title = stringResource(R.string.qr_section_event_title)) {
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
                                    contentDescription = stringResource(R.string.qr_cd_event_qr),
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
                                stringResource(R.string.qr_entry_gate, data.accessRules.entryGate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4B5563)
                            )

                            val status = if (data.accessRules.isExpired()) {
                                stringResource(R.string.qr_expired)
                            } else {
                                statusLabel(data.ticket.status)
                            }
                            val statusColor = when {
                                data.accessRules.isExpired() -> Color(0xFFD32F2F)
                                data.ticket.status == TicketStatus.ACTIVE -> Color(0xFF1F7A4D)
                                else -> Color(0xFF6B7280)
                            }
                            Text(
                                stringResource(R.string.qr_status, status),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
                        stringResource(R.string.qr_events_saved_count, existingEventList.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )

                    existingEventList.reversed().forEachIndexed { index, qr ->
                        val reversedIndex = existingEventList.size - index
                        SectionCard(title = stringResource(R.string.qr_event_number_title, reversedIndex, qr.eventSummary.title)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
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
                                        stringResource(R.string.qr_entry_gate, qr.accessRules.entryGate),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { selectedQrForView = qr },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF1A73E8),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(stringResource(R.string.common_view), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Button(
                                            onClick = {
                                                editingEvent = qr
                                                val eventSummary = qr.eventSummary
                                                val userSummary = qr.userSummary
                                                val accessRules = qr.accessRules

                                                val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                                val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                                                val isoDateFormatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

                                                val parsedDate = try {
                                                    java.time.LocalDate.parse(eventSummary.startsAt, isoDateFormatter)
                                                } catch (e: Exception) {
                                                    java.time.LocalDate.now().plusDays(7)
                                                }

                                                val parsedStartTime = try {
                                                    java.time.LocalTime.parse(accessRules.validFrom, timeFormatter)
                                                } catch (e: Exception) {
                                                    java.time.LocalTime.of(18, 0)
                                                }

                                                val parsedEndTime = try {
                                                    java.time.LocalTime.parse(accessRules.validUntil, timeFormatter)
                                                } catch (e: Exception) {
                                                    java.time.LocalTime.of(20, 0)
                                                }

                                                val category = EventCategory.values().find { it.name == eventSummary.category } ?: EventCategory.DEPORTIVO

                                                form = EventFormData(
                                                    organizerName = userSummary?.studentName ?: context.getString(R.string.qr_default_organizer_name),
                                                    organizerId = userSummary?.studentId ?: context.getString(R.string.qr_default_organizer_id),
                                                    organizerFaculty = userSummary?.faculty ?: context.getString(R.string.qr_default_faculty),
                                                    title = eventSummary.title,
                                                    category = category,
                                                    venue = eventSummary.venue,
                                                    date = parsedDate,
                                                    startTime = parsedStartTime,
                                                    endTime = parsedEndTime,
                                                    capacity = 100,
                                                    eventId = qr.ticketId
                                                )

                                                dateText = parsedDate.format(dateFormatter)
                                                startTimeText = parsedStartTime.format(timeFormatter)
                                                endTimeText = parsedEndTime.format(timeFormatter)
                                                capacityText = "100"

                                                qrCreated = false
                                                createdQrData = null
                                                showForm = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF1A73E8),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(stringResource(R.string.common_edit), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Button(
                                            onClick = { itemToDelete = qr },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFD32F2F),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(stringResource(R.string.common_delete), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
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
                    SectionCard(title = stringResource(R.string.qr_section_your_event)) {
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
                                    contentDescription = stringResource(R.string.qr_cd_event_qr),
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
                                stringResource(R.string.qr_entry_gate, data.accessRules.entryGate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4B5563)
                            )

                            val status = if (data.accessRules.isExpired()) {
                                stringResource(R.string.qr_expired)
                            } else {
                                statusLabel(data.ticket.status)
                            }
                            val statusColor = when {
                                data.accessRules.isExpired() -> Color(0xFFD32F2F)
                                data.ticket.status == TicketStatus.ACTIVE -> Color(0xFF1F7A4D)
                                else -> Color(0xFF6B7280)
                            }
                            Text(
                                stringResource(R.string.qr_status, status),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }

                    Text(
                        stringResource(R.string.qr_save_hint),
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
                        eventPreferences.clearAll()
                        existingEventList = qrPreferences.getQrList()
                        showExistingQrDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(stringResource(R.string.qr_delete_all), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExistingQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A73E8))
                ) {
                    Text(stringResource(R.string.common_cancel), fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(stringResource(R.string.qr_limit_dialog_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
                    Text(stringResource(R.string.qr_limit_dialog_message))
                    existingEventList.reversed().forEachIndexed { index, qr ->
                        val reversedIndex = existingEventList.size - index
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.qr_event_number, reversedIndex, qr.eventSummary.title))
                            Button(
                                onClick = {
                                    itemToDelete = qr
                                    showExistingQrDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(stringResource(R.string.common_delete), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        )
    }

    // Diálogo de confirmación de eliminación individual
    if (itemToDelete != null) {
        val target = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            confirmButton = {
                Button(
                    onClick = {
                        // Sincronización robusta por múltiples claves (ticketId, token, title) para registros nuevos y antiguos
                        qrPreferences.removeQr(target.ticketId)
                        qrPreferences.removeQr(target.qr.token)
                        qrPreferences.removeQr(target.eventSummary.title)

                        eventPreferences.removeEvent(target.ticketId)
                        eventPreferences.removeEvent(target.qr.token)
                        eventPreferences.removeEvent(target.eventSummary.title)

                        existingEventList = qrPreferences.getQrList()
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(stringResource(R.string.common_delete), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { itemToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A73E8))
                ) {
                    Text(stringResource(R.string.common_cancel), fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(stringResource(R.string.qr_delete_dialog_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(stringResource(R.string.qr_delete_dialog_message, target.eventSummary.title))
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
                    Text(stringResource(R.string.qr_edit_dialog_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.qr_edit_dialog_message), textAlign = TextAlign.Center)

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
                            Text(stringResource(R.string.qr_delete_qr), color = Color.White, fontWeight = FontWeight.Bold)
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
                                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.qr_cd_edit))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.qr_edit_qr), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = { showEditDialog = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A73E8))
                        ) {
                            Text(stringResource(R.string.common_cancel), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCategory.displayName(): String = when (this) {
    EventCategory.DEPORTIVO -> stringResource(R.string.qr_category_sports)
    EventCategory.ACADEMICO -> stringResource(R.string.qr_category_academic)
    EventCategory.CULTURAL_ARTISTICA -> stringResource(R.string.qr_category_cultural)
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
            contentDescription = stringResource(R.string.qr_cd_event_qr),
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
        stringResource(R.string.qr_entry_gate, data.accessRules.entryGate),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF4B5563)
    )

    val status = if (data.accessRules.isExpired()) {
        stringResource(R.string.qr_expired)
    } else {
        statusLabel(data.ticket.status)
    }
    val statusColor = when {
        data.accessRules.isExpired() -> Color(0xFFD32F2F)
        data.ticket.status == TicketStatus.ACTIVE -> Color(0xFF1F7A4D)
        else -> Color(0xFF6B7280)
    }
    Text(
        stringResource(R.string.qr_status, status),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = statusColor
    )
}

@Composable
private fun statusLabel(status: TicketStatus): String = when (status) {
    TicketStatus.ACTIVE -> stringResource(R.string.qr_status_active)
    TicketStatus.USED -> stringResource(R.string.qr_status_used)
    TicketStatus.CANCELLED -> stringResource(R.string.qr_status_cancelled)
}

/** "2026-09-01T20:00:00Z" -> "01/09/2026". */
private fun formatIsoDate(iso: String): String {
    val datePart = iso.substringBefore("T")
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else datePart
}