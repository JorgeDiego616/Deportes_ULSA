package com.ulsa.deportes.ui.qrSection.qrHome.network

import com.ulsa.deportes.ui.qrSection.qrHome.model.AccessRules
import com.ulsa.deportes.ui.qrSection.qrHome.model.CapacityStatus
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrRequest
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse
import com.ulsa.deportes.ui.qrSection.qrHome.model.EventSummary
import com.ulsa.deportes.ui.qrSection.qrHome.model.QrInfo
import com.ulsa.deportes.ui.qrSection.qrHome.model.TicketInfo
import com.ulsa.deportes.ui.qrSection.qrHome.model.TicketStatus
import com.ulsa.deportes.ui.qrSection.qrHome.model.UserSummary
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Implementación simulada de [QrApi] para poder probar toda la pantalla (incluida
 * la generación real del QR con ZXing) sin backend.
 *
 * El token es opaco para el cliente: aquí es un simple texto aleatorio, pero para
 * el generador de QR es suficiente.
 */
class MockQrApi : QrApi {

    override suspend fun createQr(request: CreateQrRequest): CreateQrResponse {
        delay(600) // simula latencia de red

        return CreateQrResponse(
            ticketId = "tkt_" + UUID.randomUUID().toString().take(8),
            qr = QrInfo(
                // Los pases estáticos no se renuevan: refreshAt = null.
                token = "mock." + UUID.randomUUID().toString().replace("-", ""),
                refreshAt = null
            ),
            ticket = TicketInfo(status = TicketStatus.ACTIVE),
            eventSummary = EventSummary(
                // Eco del formulario; el backend real traería estos datos del evento.
                title = request.eventTitle.ifBlank { "Clásico ULSA 2026" },
                category = request.eventCategory.ifBlank { "Fútbol" },
                venue = request.eventVenue.ifBlank { "Estadio La Salle" },
                startsAt = request.validFrom
            ),
            userSummary = UserSummary(
                studentName = request.studentName.ifBlank { "Jorge Chaparro" },
                studentId = request.studentId.ifBlank { "14446" },
                faculty = request.faculty.ifBlank { "Ingeniería" }
            ),
            accessRules = AccessRules(
                entryGate = "Puerta Norte",
                validFrom = request.validFrom,
                validUntil = request.validUntil
            )
        )
    }

    override suspend fun getCapacity(eventId: String): CapacityStatus {
        delay(300)
        return CapacityStatus(capacityLimit = 500, spotsRemaining = 137)
    }
}
