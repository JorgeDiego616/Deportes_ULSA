package com.ulsa.deportes.ui.qrSection.qrHome.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse

private const val KEY_QR_LIST = "qr_saved_list"
private const val MAX_QR_COUNT = 3

/**
 * Persistencia de códigos QR generados (máximo 3).
 * Guarda una lista de [CreateQrResponse] en SharedPreferences como JSON.
 */
class QrPreferences(context: Context) {

    private val appPreferences = AppPreferences(context)
    private val gson = Gson()
    private val listType = object : TypeToken<List<CreateQrResponse>>() {}.type

    /** Guarda un nuevo QR en la lista (máximo 3, elimina el más antiguo si se pasa). */
    fun saveQr(response: CreateQrResponse) {
        val currentList = getQrList()
        val updatedList = (currentList + response).takeLast(MAX_QR_COUNT)
        val json = gson.toJson(updatedList)
        appPreferences.putString(KEY_QR_LIST, json)
    }

    /** Obtiene la lista completa de QRs guardados (más reciente al final). */
    fun getQrList(): List<CreateQrResponse> {
        val json = appPreferences.getString(KEY_QR_LIST)
        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            gson.fromJson(json, listType)
        }
    }

    /** Obtiene el QR más reciente. */
    fun getLatestQr(): CreateQrResponse? = getQrList().lastOrNull()

    /** Verifica si hay al menos un QR guardado. */
    fun hasQr(): Boolean = getQrList().isNotEmpty()

    /** Verifica si se alcanzó el límite máximo. */
    fun isAtMaxLimit(): Boolean = getQrList().size >= MAX_QR_COUNT

    /** Elimina un QR específico por ticketId. */
    fun removeQr(ticketId: String) {
        val currentList = getQrList().filter { it.ticketId != ticketId }
        val json = gson.toJson(currentList)
        appPreferences.putString(KEY_QR_LIST, json)
    }

    /** Elimina todos los QRs. */
    fun clearAll() {
        appPreferences.remove(KEY_QR_LIST)
    }
}