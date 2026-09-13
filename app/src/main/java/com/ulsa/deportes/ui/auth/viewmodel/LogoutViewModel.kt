package com.ulsa.deportes.ui.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ulsa.deportes.ui.auth.data.SessionPreferences

/**
 * ViewModel para cerrar sesión.
 *
 * El backend actual (GraphQL + JWT stateless) no expone ninguna mutation de
 * logout — solo `login` y `registrarUsuario` (ver `schema.graphql` del servicio
 * de seguridad). Por eso cerrar sesión es 100% del lado del cliente: se borra
 * el token guardado en el dispositivo y punto.
 *
 * El JWT en sí sigue siendo técnicamente válido en el servidor hasta que expira
 * por su cuenta (8h, ver `expiresIn: '8h'` en el resolver de login) — no hay
 * forma de "revocarlo" antes sin agregar soporte de logout al backend (ver nota
 * al final del archivo si en algún momento se necesita esa versión más segura).
 */
class LogoutViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionPreferences(application)

    /**
     * Cierra la sesión local al instante e invoca [onLoggedOut] (para navegar
     * a "login"). No hay llamada de red: no hay nada que avisarle al servidor.
     */
    fun logout(onLoggedOut: () -> Unit) {
        session.clearSession()
        onLoggedOut()
    }
}

/*
 * Si en el futuro el equipo agrega una mutation `logout` real al backend
 * (invalidando el token en Redis, por ejemplo), este ViewModel volvería a
 * necesitar una llamada de red "best-effort" en segundo plano, igual al
 * patrón que tenía antes con el backend de Django (logoutScope +
 * viewModelScope aparte, para que la navegación no cancele la petición).
 */