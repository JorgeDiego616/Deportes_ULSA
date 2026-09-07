package com.ulsa.deportes.common.preferences

import android.content.Context

private const val PREFS_NAME = "app_prefs"

/**
 * Punto único de acceso a [android.content.SharedPreferences] para toda la app: un solo
 * archivo de preferencias y una API común, para que ninguna feature tenga que llamar
 * directo a `context.getSharedPreferences(...)` ni repetir el nombre del archivo.
 *
 * Cada feature que necesite persistir algo debe crear su propia clase pequeña (p. ej.
 * `OnboardingPreferences`) que use esta como base y exponga métodos con nombres de dominio.
 */
class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getString(key: String, default: String? = null): String? =
        prefs.getString(key, default)

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun remove(vararg keys: String) {
        prefs.edit().apply { keys.forEach { remove(it) } }.apply()
    }
}