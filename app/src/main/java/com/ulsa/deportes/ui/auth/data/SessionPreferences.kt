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
     * Verifica si la sesión es válida (existe token y no ha expirado).
     * Decodifica el JWT y revisa el claim `exp` comparándolo con la hora actual.
     * No verifica la firma del token (eso requiere el backend).
     */
    fun isSessionValid(): Boolean {
        val token = refreshToken() ?: return false
        return try {
            val payload = decodeJwtPayload(token)
            val exp = payload.getLong("exp")
            val now = System.currentTimeMillis() / 1000
            exp > now
        } catch (e: Exception) {
            false
        }
    }

    private fun decodeJwtPayload(token: String): JSONObject {
        val parts = token.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 3) throw IllegalArgumentException("JWT inválido")
        val payload = parts[1]
        val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        return JSONObject(String(decoded))
    }

    /** Borra la sesión del dispositivo (logout). */
    fun clearSession() {
        appPreferences.remove(KEY_ACCESS, KEY_REFRESH, KEY_USERNAME)
    }
}