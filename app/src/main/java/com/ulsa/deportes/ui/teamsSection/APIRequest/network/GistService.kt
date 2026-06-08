package com.ulsa.deportes.ui.teamsSection.APIRequest.network

import com.ulsa.deportes.ui.teamsSection.APIRequest.model.TeamsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Interfaz de servicio Retrofit para obtener datos de estudiantes desde GitHub Gist.
 *
 * Define los endpoints HTTP disponibles utilizando anotaciones de Retrofit.
 * Todas las funciones son `suspend` para permitir llamadas asincrónicas con corrutinas.
 *
 * @see Retrofit
 * @see GsonConverterFactory
 */
interface GistService {
    /**
     * Realiza una petición GET para obtener la lista de estudiantes.
     *
     * El endpoint apunta a un archivo JSON alojado en GitHub Gist.
     *
     * URL completa:
     * https://gist.githubusercontent.com/ingjromo/5f3c4549cfc64984c9131112fd87d718/raw/6e36ceff88f29b046f2d8529041526a05b8b3dde/students26.json
     *
     * @return [StudentsResponse] Objeto deserializado desde el JSON de la API
     *
     * Ejemplo de respuesta JSON:
     * ```json
     * {
     *   "students": [
     *     {
     *       "name": "Juan Pérez",
     *       "studentId": "STU001",
     *       "hobby": "Programación",
     *       "photoUrl": "https://example.com/photos/juan.jpg"
     *     }
     *   ]
     * }
     * ```
     *
     * @throws IOException Si hay error de conectividad
     * @throws HttpException Si el servidor devuelve un código de error HTTP
     */
    @GET("https://gist.githubusercontent.com/JorgeDiego616/ce74caaa973c2cb2cc4c2212ff263236/raw/4cfa6f879ad26ca7a09bb11848ea71c61503340a/teams.json")
    suspend fun getTeams(): TeamsResponse
}

/**
 * Singleton que gestiona la configuración y creación del cliente Retrofit.
 *
 * Proporciona una instancia única de [GistService] durante toda la vida de la aplicación.
 *
 * Configuración:
 * - Base URL: https://gist.githubusercontent.com/
 * - Converter: GsonConverterFactory para deserializar JSON a objetos Kotlin
 *
 * Uso:
 * ```kotlin
 * val response = RetrofitClient.gistService.getStudents()
 * ```
 */
object RetrofitClient {
    /**
     * Instancia singleton del servicio Retrofit configurado.
     *
     * Se crea lazy (de forma perezosa) la primera vez que se accede,
     * y se reutiliza en todas las peticiones posteriores.
     */
    val gistService: GistService = Retrofit.Builder()
        .baseUrl("https://gist.githubusercontent.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GistService::class.java)
}