package com.ulsa.deportes.ui.auth.data

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ulsa.deportes.ui.auth.model.UserDto
import org.json.JSONObject

private const val KEY_TOKEN = "auth_token"
private const val KEY_USERNAME = "auth_username"
private const val SECURE_PREFS_NAME = "secure_auth_prefs"

/**
 * Gestiona de forma segura la sesión del usuario utilizando almacenamiento cifrado
 * (EncryptedSharedPreferences con AES-256 GCM).
 * No permite almacenamiento en texto plano ni utiliza fallback inseguro.
 */
class SessionPreferences(context: Context) {

    private val prefs: android.content.SharedPreferences = try {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Por seguridad, si falla la inicialización del almacenamiento cifrado,
        // no guardamos credenciales en texto plano. Lanzamos un error controlado.
        throw IllegalStateException("Fallo crítico en almacenamiento seguro de sesión: ${e.message}", e)
    }

    /** Guarda la sesión (token JWT y correo de usuario) cifrada. */
    fun saveSession(token: String, user: UserDto) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_USERNAME, user.email).apply()
    }

    fun accessToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun username(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /** Hay sesión activa si existe token válido y no ha expirado. */
    fun isLoggedIn(): Boolean = isSessionValid()

    /**
     * Valida si la sesión es vigente verificando el claim `exp` del JWT.
     * No valida la firma criptográfica en el cliente.
     */
    fun isSessionValid(): Boolean {
        val token = accessToken() ?: return false
        return try {
            val payload = decodeJwtPayload(token)
            val exp = payload.getLong("exp")
            val now = System.currentTimeMillis() / 1000
            exp > now
        } catch (_: Exception) {
            false
        }
    }

    private fun decodeJwtPayload(token: String): JSONObject {
        val parts = token.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 3) throw IllegalArgumentException("JWT inválido")
        val payload = parts[1]
        val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        return JSONObject(String(decoded))
    }

    /** Borra la sesión del dispositivo (logout). */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
