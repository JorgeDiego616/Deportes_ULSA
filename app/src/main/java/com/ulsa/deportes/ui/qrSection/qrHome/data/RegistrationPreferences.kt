package com.ulsa.deportes.ui.qrSection.qrHome.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.RegistrationResponse

private const val KEY_REGISTRATION_LIST = "registration_saved_list"

/**
 * Persistencia de registros de asistentes.
 */
class RegistrationPreferences(context: Context) {

    private val appPreferences = AppPreferences(context)
    private val gson = Gson()
    private val listType = object : TypeToken<List<RegistrationResponse>>() {}.type

    fun saveRegistration(response: RegistrationResponse) {
        val currentList = getRegistrationList()
        val updatedList = currentList + response
        val json = gson.toJson(updatedList)
        appPreferences.putString(KEY_REGISTRATION_LIST, json)
    }

    fun getRegistrationList(): List<RegistrationResponse> {
        val json = appPreferences.getString(KEY_REGISTRATION_LIST)
        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            gson.fromJson(json, listType)
        }
    }

    fun getRegistrationsForEvent(eventId: String): List<RegistrationResponse> =
        getRegistrationList().filter { it.eventId == eventId }

    fun clearAll() {
        appPreferences.remove(KEY_REGISTRATION_LIST)
    }
}