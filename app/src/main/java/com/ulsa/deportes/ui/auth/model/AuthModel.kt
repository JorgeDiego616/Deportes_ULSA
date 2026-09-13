package com.ulsa.deportes.ui.auth.model

/**
 * Datos que viajan hacia y desde la API de autenticación (GraphQL vía el Gateway
 * de Apollo Federation · servicio "seguridad" · http://10.0.2.2:4000/ en el emulador).
 *
 * A diferencia de una API REST, aquí NO hay un endpoint por acción: todo se manda
 * como un único POST con un "sobre" GraphQL ([GraphQLRequest]), y el servidor
 * siempre responde con el mismo "sobre" ([GraphQLResponse]).
 *
 * Solo contiene datos: ni lógica de red ni de UI. La lógica vive en los ViewModel
 * de `ui/auth/viewmodel/` y la persistencia en `ui/auth/data/SessionPreferences`.
 */

// ---------------------------------------------------------------------------
// Sobres genéricos de GraphQL (se reutilizan para login, logout, o cualquier
// otra operación futura contra este backend).
// ---------------------------------------------------------------------------

/** Cuerpo de CUALQUIER petición GraphQL: la operación como texto + sus variables. */
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any?>
)

/** Respuesta de CUALQUIER petición GraphQL: o viene `data`, o vienen `errors`. */
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<GraphQLError>?
)

/** Un error de GraphQL (ej. "Credenciales inválidas."). */
data class GraphQLError(
    val message: String
)

// ---------------------------------------------------------------------------
// Específico de la operación "login"
// ---------------------------------------------------------------------------

/** Lo que pide la mutation `login(email, password)`. */
data class LoginVariables(
    val email: String,
    val password: String
)

/** El campo `data.login` de la respuesta. */
data class LoginData(
    val login: LoginPayload
)

/** El `token` (JWT, 8h de vigencia) + los datos del usuario. */
data class LoginPayload(
    val token: String,
    val usuario: UserDto
)

/**
 * Datos del usuario devueltos por el backend. El `id` de Mongo es un string
 * (ej. "6aa618ad55df53d56a0b022f"), no un entero como en el backend anterior.
 */
data class UserDto(
    val id: String,
    val matricula: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean
)

// ---------------------------------------------------------------------------
// Estado observable de la pantalla de login (sin cambios respecto a antes)
// ---------------------------------------------------------------------------

/**
 * Estado observable de la pantalla de login. Mismo patrón que `OnboardingUiState`.
 *
 * @property isLoading true mientras se hace la petición (mostrar spinner).
 * @property error mensaje a mostrar bajo el formulario, o null si no hay error.
 * @property isLoggedIn pasa a true cuando el login fue correcto y ya se guardó la
 *   sesión; la vista lo observa para navegar a "tabs".
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)