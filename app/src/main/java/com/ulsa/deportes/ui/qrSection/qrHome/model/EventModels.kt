package com.ulsa.deportes.ui.qrSection.qrHome.model

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Modelos del módulo de Eventos (antes QR).
 * Ahora maneja creación de eventos con fecha/hora y registro de asistentes.
 */

enum class EventCategory {
    @SerializedName("deportivo") DEPORTIVO,
    @SerializedName("academico") ACADEMICO,
    @SerializedName("cultural_artistica") CULTURAL_ARTISTICA
}

// TicketStatus se usa desde QrModels.kt

data class EventRequest(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: EventCategory,
    @SerializedName("venue") val venue: String,
    @SerializedName("date") val date: String,           // yyyy-MM-dd
    @SerializedName("start_time") val startTime: String, // HH:mm
    @SerializedName("end_time") val endTime: String,     // HH:mm
    @SerializedName("organizer_name") val organizerName: String,
    @SerializedName("organizer_id") val organizerId: String,
    @SerializedName("organizer_faculty") val organizerFaculty: String,
    @SerializedName("capacity") val capacity: Int = 100
)

data class EventResponse(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: EventCategory,
    @SerializedName("venue") val venue: String,
    @SerializedName("date") val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("organizer_name") val organizerName: String,
    @SerializedName("organizer_id") val organizerId: String,
    @SerializedName("organizer_faculty") val organizerFaculty: String,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("qr_token") val qrToken: String,
    @SerializedName("created_at") val createdAt: String
)

data class RegistrationRequest(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("attendee_name") val attendeeName: String,
    @SerializedName("attendee_email") val attendeeEmail: String
)

data class RegistrationResponse(
    @SerializedName("registration_id") val registrationId: String,
    @SerializedName("event_id") val eventId: String,
    @SerializedName("attendee_name") val attendeeName: String,
    @SerializedName("attendee_email") val attendeeEmail: String,
    @SerializedName("qr_token") val qrToken: String,
    @SerializedName("ticket_status") val ticketStatus: TicketStatus,
    @SerializedName("registered_at") val registeredAt: String
)

data class EventFormData(
    val organizerName: String,
    val organizerId: String,
    val organizerFaculty: String,
    val title: String,
    val category: EventCategory,
    val venue: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val capacity: Int,
    val eventId: String = UUID.randomUUID().toString()
) {
    fun toRequest(): EventRequest = EventRequest(
        eventId = eventId,
        title = title,
        category = category,
        venue = venue,
        date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        startTime = startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
        endTime = endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
        organizerName = organizerName,
        organizerId = organizerId,
        organizerFaculty = organizerFaculty,
        capacity = capacity
    )
}

object DemoEventForm {
    fun defaults(): EventFormData = EventFormData(
        organizerName = "Jorge Chaparro",
        organizerId = "14446",
        organizerFaculty = "Ingeniería",
        title = "Clásico ULSA 2026",
        category = EventCategory.DEPORTIVO,
        venue = "Estadio La Salle",
        date = LocalDate.now().plusDays(7),
        startTime = LocalTime.of(18, 0),
        endTime = LocalTime.of(20, 0),
        capacity = 100
    )
}

/** Formato legible para mostrar en UI. */
fun EventFormData.getFormattedDate(): String =
    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

fun EventFormData.getFormattedTimeRange(): String =
    "${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"

fun EventResponse.getFormattedDate(): String {
    return try {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) { date }
}

fun EventResponse.getFormattedTimeRange(): String =
    "$startTime - $endTime"