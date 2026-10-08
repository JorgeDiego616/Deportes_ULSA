package com.ulsa.deportes.ui.qrSection.qrHome.data

import android.content.Context
import com.google.gson.Gson
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse

private const val KEY_QR_RESPONSE = "qr_saved_response"

/**
 * Persistencia del código QR generado.
 * Guarda el [CreateQrResponse] completo en SharedPreferences como JSON.
 */
class QrPreferences(context: Context) {

    private val appPreferences = AppPreferences(context)
    private val gson = Gson()

    /** Guarda el QR generado tras crearlo exitosamente. */
    fun saveQr(response: CreateQrResponse) {
        val json = gson.toJson(response)
        appPreferences.putString(KEY_QR_RESPONSE, json)
    }

    /** Obtiene el QR guardado, o null si no existe. */
    fun getQr(): CreateQrResponse? {
        val json = appPreferences.getString(KEY_QR_RESPONSE)
        return if (json.isNullOrBlank()) null else gson.fromJson(json, CreateQrResponse::class.java)
    }

    /** Verifica si hay un QR guardado. */
    fun hasQr(): Boolean = !appPreferences.getString(KEY_QR_RESPONSE).isNullOrBlank()

    /** Elimina el QR guardado. */
    fun clearQr() {
        appPreferences.remove(KEY_QR_RESPONSE)
    }
}