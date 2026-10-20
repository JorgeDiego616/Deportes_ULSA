package com.ulsa.deportes.ui.qrSection.qrHome.network

import android.content.Context
import com.ulsa.deportes.ui.auth.data.SessionPreferences

/**
 * Punto único para construir la implementación de [QrApi].
 *
 * Cuando el backend de QR esté disponible (endpoints /qr y /events/{id}/capacity),
 * basta con poner [USE_MOCK] en false: el cliente real ya usa la URL del gateway
 * (http://10.0.2.2:4000/) y el token de sesión del login.
 */
object QrDependencies {

    /** true = datos simulados locales (el backend aún no tiene los endpoints). */
    const val USE_MOCK = true

    fun createApi(context: Context): QrApi =
        if (USE_MOCK) {
            MockQrApi()
        } else {
            val appContext = context.applicationContext
            QrApiClient.create { SessionPreferences(appContext).accessToken() }
        }
}
