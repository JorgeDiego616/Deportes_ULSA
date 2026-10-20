package com.ulsa.deportes.ui.qrSection.qrHome.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.EventResponse

private const val KEY_EVENT_LIST = "event_saved_list"
private const val MAX_EVENT_COUNT = 3
private const val KEY_ORG_NAME = "org_name_default"
private const val KEY_ORG_ID = "org_id_default"
private const val KEY_ORG_FACULTY = "org_faculty_default"

/**
 * Persistencia de eventos creados (máximo 3).
 */
class EventPreferences(context: Context) {

    private val appPreferences = AppPreferences(context)
    private val gson = Gson()
    private val listType = object : TypeToken<List<EventResponse>>() {}.type

    /** Guarda los datos del organizador por defecto para persistir el último valor ingresado. */
    fun saveOrganizerDefaults(name: String, id: String, faculty: String) {
        appPreferences.putString(KEY_ORG_NAME, name)
        appPreferences.putString(KEY_ORG_ID, id)
        appPreferences.putString(KEY_ORG_FACULTY, faculty)
    }

    /** Obtiene el nombre del organizador guardado o el valor por defecto ("Jorge Chaparro"). */
    fun getOrganizerName(default: String = "Jorge Chaparro"): String =
        appPreferences.getString(KEY_ORG_NAME)?.takeIf { it.isNotBlank() } ?: default

    /** Obtiene el ID del organizador guardado o el valor por defecto ("14446"). */
    fun getOrganizerId(default: String = "14446"): String =
        appPreferences.getString(KEY_ORG_ID)?.takeIf { it.isNotBlank() } ?: default

    /** Obtiene la facultad del organizador guardada o el valor por defecto ("Ingeniería"). */
    fun getOrganizerFaculty(default: String = "Ingeniería"): String =
        appPreferences.getString(KEY_ORG_FACULTY)?.takeIf { it.isNotBlank() } ?: default

    /**
     * Verifica si se puede guardar un evento (si ya existe para edición, o si hay espacio < 3).
     */
    fun canSave(identifier: String): Boolean {
        val currentList = getEventList()
        val isExisting = currentList.any { it.eventId == identifier || it.qrToken == identifier }
        return isExisting || currentList.size < MAX_EVENT_COUNT
    }

    /**
     * Guarda un nuevo evento en la lista si no se ha alcanzado el límite máximo (3).
     * @return true si se guardó exitosamente, false si se bloqueó por alcanzar el límite.
     */
    fun saveEvent(response: EventResponse): Boolean {
        val currentList = getEventList()
        // Si el evento ya existe (edición), actualízalo en su posición sin aumentar el contador
        val existingIndex = currentList.indexOfFirst { it.eventId == response.eventId || it.qrToken == response.qrToken }
        if (existingIndex >= 0) {
            val updatedList = currentList.toMutableList().apply {
                this[existingIndex] = response
            }
            val json = gson.toJson(updatedList)
            appPreferences.putString(KEY_EVENT_LIST, json)
            return true
        }

        // Si es un evento nuevo y ya se alcanzó el límite, no eliminar nada y retornar false
        if (currentList.size >= MAX_EVENT_COUNT) {
            return false
        }

        val updatedList = currentList + response
        val json = gson.toJson(updatedList)
        appPreferences.putString(KEY_EVENT_LIST, json)
        return true
    }

    /** Obtiene la lista completa de eventos guardados (más reciente al final). */
    fun getEventList(): List<EventResponse> {
        val json = appPreferences.getString(KEY_EVENT_LIST)
        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            gson.fromJson(json, listType)
        }
    }

    /** Obtiene el evento más reciente. */
    fun getLatestEvent(): EventResponse? = getEventList().lastOrNull()

    /** Verifica si hay al menos un evento guardado. */
    fun hasEvents(): Boolean = getEventList().isNotEmpty()

    /** Verifica si se alcanzó el límite máximo. */
    fun isAtMaxLimit(): Boolean = getEventList().size >= MAX_EVENT_COUNT

    /** Elimina un evento específico por id único (eventId o qrToken). */
    fun removeEvent(id: String) {
        val currentList = getEventList()
        val updatedList = currentList.filter { it.eventId != id && it.qrToken != id }
        val json = gson.toJson(updatedList)
        appPreferences.putString(KEY_EVENT_LIST, json)
    }

    /** Restaura una lista específica (útil para rollback). */
    fun restoreList(list: List<EventResponse>) {
        val json = gson.toJson(list)
        appPreferences.putString(KEY_EVENT_LIST, json)
    }

    /** Elimina todos los eventos. */
    fun clearAll() {
        appPreferences.remove(KEY_EVENT_LIST)
    }
}
