package com.ulsa.deportes.ui.qrSection.qrHome.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.EventResponse

private const val KEY_EVENT_LIST = "event_saved_list"
private const val MAX_EVENT_COUNT = 3

/**
 * Persistencia de eventos creados (máximo 3).
 */
class EventPreferences(context: Context) {

    private val appPreferences = AppPreferences(context)
    private val gson = Gson()
    private val listType = object : TypeToken<List<EventResponse>>() {}.type

    /** Guarda un nuevo evento en la lista (máximo 3, elimina el más antiguo si se pasa). */
    fun saveEvent(response: EventResponse) {
        val currentList = getEventList()
        val updatedList = (currentList + response).takeLast(MAX_EVENT_COUNT)
        val json = gson.toJson(updatedList)
        appPreferences.putString(KEY_EVENT_LIST, json)
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

    /** Elimina un evento específico por eventId. */
    fun removeEvent(eventId: String) {
        val currentList = getEventList().filter { it.eventId != eventId }
        val json = gson.toJson(currentList)
        appPreferences.putString(KEY_EVENT_LIST, json)
    }

    /** Elimina todos los eventos. */
    fun clearAll() {
        appPreferences.remove(KEY_EVENT_LIST)
    }
}