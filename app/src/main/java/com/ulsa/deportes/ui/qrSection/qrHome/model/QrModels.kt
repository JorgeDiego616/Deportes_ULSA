package com.ulsa.deportes.ui.qrSection.qrHome.model

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Modelos del módulo de códigos QR para eventos.
 *
 * Se usan anotaciones de Gson (@SerializedName) para respetar el contrato JSON
 * snake_case del backend, igual que el resto de la app. Las enums también llevan
 * @SerializedName para enviar/recibir los valores correctos (ej. static_event_checkin).
 */

enum class CreationMode {
    @SerializedName("dynamic_individual_pass")
    DYNAMIC_INDIVIDUAL_PASS,

    @SerializedName("static_event_checkin")
    STATIC_EVENT_CHECKIN
}

enum class TicketStatus {
    @SerializedName("active") ACTIVE,
    @SerializedName("used") USED,
    @SerializedName("cancelled") CANCELLED
}

data class Geofence(
    val lat: Double,
    val lng: Double,
    @SerializedName("radius_m") val radiusM: Int
)

data class SecurityConfig(
    @SerializedName("qr_ttl_seconds") val qrTtlSeconds: Int? = null,
    val geofence: Geofence? = null
)

/**
 * Cuerpo de la petición para crear un QR.
 *
 * [userId] es obligatorio solo en modo dinámico; [validUntil] es el límite duro
 * del ticket y nunca debe superarlo el TTL dinámico.
 */
data class CreateQrRequest(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("creation_mode") val creationMode: CreationMode,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("valid_from") val validFrom: String,
    @SerializedName("valid_until") val validUntil: String,
    @SerializedName("security_config") val securityConfig: SecurityConfig = SecurityConfig(),
    // Datos descriptivos del evento y del portador. Hoy los llena el formulario
    // de la pantalla de QR (y el mock los devuelve en event_summary /
    // user_summary). Cuando el backend exista, serán los inputs de la mutation
    // `crearQR` (ver QR-CONTEXTO.md, sección 11).
    @SerializedName("event_title") val eventTitle: String = "",
    @SerializedName("event_category") val eventCategory: String = "",
    @SerializedName("event_venue") val eventVenue: String = "",
    @SerializedName("student_name") val studentName: String = "",
    @SerializedName("student_id") val studentId: String = "",
    @SerializedName("faculty") val faculty: String = "",
) {
    init {
        require(creationMode != CreationMode.DYNAMIC_INDIVIDUAL_PASS || userId != null) {
            "user_id es obligatorio en dynamic_individual_pass"
        }
    }
}

data class QrInfo(
    val token: String,
    @SerializedName("refresh_at") val refreshAt: Long? = null
)

data class TicketInfo(
    val status: TicketStatus,
    @SerializedName("used_at") val usedAt: String? = null,
    @SerializedName("used_at_gate") val usedAtGate: String? = null
)

data class EventSummary(
    val title: String,
    val category: String,
    val venue: String,
    @SerializedName("starts_at") val startsAt: String
)

data class UserSummary(
    @SerializedName("student_name") val studentName: String,
    @SerializedName("student_id") val studentId: String,
    val faculty: String
)

data class AccessRules(
    @SerializedName("entry_gate") val entryGate: String,
    @SerializedName("valid_from") val validFrom: String,
    @SerializedName("valid_until") val validUntil: String
)

data class CreateQrResponse(
    @SerializedName("ticket_id") val ticketId: String,
    val qr: QrInfo,
    val ticket: TicketInfo,
    @SerializedName("event_summary") val eventSummary: EventSummary,
    @SerializedName("user_summary") val userSummary: UserSummary? = null,
    @SerializedName("access_rules") val accessRules: AccessRules
)

data class CapacityStatus(
    @SerializedName("capacity_limit") val capacityLimit: Int,
    @SerializedName("spots_remaining") val spotsRemaining: Int
) {
    val isSoldOut: Boolean get() = spotsRemaining == 0
}

/**
 * "expired" no se guarda en el backend: se calcula comparando el momento actual
 * contra access_rules.valid_until.
 */
fun AccessRules.isExpired(now: Instant = Instant.now()): Boolean =
    now.isAfter(Instant.parse(validUntil))

/**
 * Datos editables del formulario de la pantalla de QR.
 *
 * Mientras el backend no exista, el usuario los llena aquí y viajan dentro del
 * [CreateQrRequest] para que el mock los "ecoe" en la respuesta. Cuando se
 * implemente `crearQR` en UlsaHub, este mismo objeto se convierte en los inputs
 * de la mutation GraphQL (evento + portador), así el contrato no cambia.
 */
data class QrFormData(
    val studentName: String,
    val studentId: String,
    val faculty: String,
    val eventTitle: String,
    val eventCategory: String,
    val eventVenue: String,
    val eventId: String,
    val creationMode: CreationMode,
) {
    fun toRequest(now: Instant = Instant.now()): CreateQrRequest {
        val until = now.plus(30, ChronoUnit.DAYS)
        return CreateQrRequest(
            eventId = eventId,
            creationMode = creationMode,
            userId = if (creationMode == CreationMode.DYNAMIC_INDIVIDUAL_PASS) studentId else null,
            validFrom = now.toString(),
            validUntil = until.toString(),
            eventTitle = eventTitle,
            eventCategory = eventCategory,
            eventVenue = eventVenue,
            studentName = studentName,
            studentId = studentId,
            faculty = faculty,
        )
    }
}

/** Valores iniciales del formulario (evento demo para probar toda la UI). */
object DemoQrForm {
    fun defaults(): QrFormData = QrFormData(
        studentName = "Jorge Chaparro",
        studentId = "14446",
        faculty = "Ingeniería",
        eventTitle = "Clásico ULSA 2026",
        eventCategory = "Fútbol",
        eventVenue = "Estadio La Salle",
        eventId = "evento-demo-ulsa",
        creationMode = CreationMode.STATIC_EVENT_CHECKIN,
    )
}
