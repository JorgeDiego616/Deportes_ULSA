package com.ulsa.deportes.ui.auth.data

import android.content.Context
import android.util.Base64
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.auth.model.UserDto
import org.json.JSONObject

private const val KEY_ACCESS = "auth_access"
private const val KEY_REFRESH = "auth_refresh"
private const val KEY_USERNAME = "auth_username"

/**
 * Expone, con nombres de dominio, la sesión del usuario (tokens JWT + username).
 * La persistencia real vive en [AppPreferences], igual que OnboardingPreferences.
 *
 * Nota: se guarda en `SharedPreferences` plano para mantener el estándar del
 * proyecto. Si se quisiera cifrar, el cambio queda aislado en esta clase.
 */
class SessionPreferences(context: Context) {

    private val appPreferences = AppPreferences(context)

    /** Guarda la sesión tras un login correcto. */
    fun saveSession(access: String, refresh: String, user: UserDto) {
        appPreferences.putString(KEY_ACCESS, access)
        appPreferences.putString(KEY_REFRESH, refresh)
        appPreferences.putString(KEY_USERNAME, user.email)
    }

    fun accessToken(): String? = appPreferences.getString(KEY_ACCESS)
    fun refreshToken(): String? = appPreferences.getString(KEY_REFRESH)
    fun username(): String? = appPreferences.getString(KEY_USERNAME)

    /** Hay sesión si tenemos un refresh token guardado. */
    fun isLoggedIn(): Boolean = !refreshToken().isNullOrBlank()

    /**
     * Verifica si el access token actual es válido (no ha expirado).
     * Decodifica el claim `exp` del JWT y lo compara con la hora actual.
     * No verifica la firma criptográfica; solo expiración local.
     */
    fun isTokenValid(): Boolean {
        val token = accessToken() ?: return false
        val exp = getTokenExpiration(token) ?: return false
        return System.currentTimeMillis() / 1000 < exp
    }

    /**
     * Verifica si hay una sesión válida (token existe y no ha expirado).
     * Si el token expiró, limpia la sesión automáticamente.
     */
    fun hasValidSession(): Boolean {
        if (!isLoggedIn()) return false
        if (!isTokenValid()) {
            clearSession()
            return false
        }
        return true
    }

    /** Borra la sesión del dispositivo (logout). */
    fun clearSession() {
        appPreferences.remove(KEY_ACCESS, KEY_REFRESH, KEY_USERNAME)
    }

    /**
     * Extrae el claim `exp` (expiración en segundos epoch) del JWT.
     * El JWT tiene 3 partes separadas por '.': header.payload.signature
     * La parte media (payload) está en base64url.
     */
    private fun getTokenExpiration(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = parts[1]
            // Base64 URL-safe decode (agregar padding si falta)
            val normalized = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')
                .replace('-', '+')
                .replace('_', '/')
            val decoded = Base64.decode(normalized, Base64.URL_SAFE or Base64.NO_WRAP)
            val json = JSONObject(String(decoded))
            json.getLong("exp")
        } catch (e: Exception) {
            null
        }
    }
}