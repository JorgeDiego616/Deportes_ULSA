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

    /**
     * Verifica si se puede guardar un QR (si ya existe para edición, o si hay espacio < 3).
     */
    fun canSave(identifier: String): Boolean {
        val currentList = getQrList()
        val isExisting = currentList.any { it.ticketId == identifier || it.qr.token == identifier }
        return isExisting || currentList.size < MAX_QR_COUNT
    }

    /**
     * Guarda un nuevo QR en la lista si no se ha alcanzado el límite máximo (3).
     * @return true si se guardó exitosamente, false si se bloqueó por alcanzar el límite.
     */
    fun saveQr(response: CreateQrResponse): Boolean {
        val currentList = getQrList()
        // Si el QR ya existe (edición por ticketId o token), actualízalo sin incrementar el contador
        val existingIndex = currentList.indexOfFirst { it.ticketId == response.ticketId || it.qr.token == response.qr.token }
        if (existingIndex >= 0) {
            val updatedList = currentList.toMutableList().apply {
                this[existingIndex] = response
            }
            val json = gson.toJson(updatedList)
            appPreferences.putString(KEY_QR_LIST, json)
            return true
        }

        // Si es un QR nuevo y ya se alcanzó el límite, no eliminar nada y retornar false
        if (currentList.size >= MAX_QR_COUNT) {
            return false
        }

        val updatedList = currentList + response
        val json = gson.toJson(updatedList)
        appPreferences.putString(KEY_QR_LIST, json)
        return true
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

    /** Elimina un QR específico por id único (ticketId o token). */
    fun removeQr(id: String) {
        val currentList = getQrList()
        val updatedList = currentList.filter { it.ticketId != id && it.qr.token != id }
        val json = gson.toJson(updatedList)
        appPreferences.putString(KEY_QR_LIST, json)
    }

    /** Restaura una lista específica (útil para rollback). */
    fun restoreList(list: List<CreateQrResponse>) {
        val json = gson.toJson(list)
        appPreferences.putString(KEY_QR_LIST, json)
    }

    /** Elimina todos los QRs. */
    fun clearAll() {
        appPreferences.remove(KEY_QR_LIST)
    }
}
