package com.ulsa.deportes.ui.auth.network

import com.ulsa.deportes.ui.auth.model.GraphQLRequest
import com.ulsa.deportes.ui.auth.model.GraphQLResponse
import com.ulsa.deportes.ui.auth.model.LoginData
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Endpoint único de la API de autenticación (GraphQL vía el Gateway de Apollo
 * Federation, servicio "seguridad").
 *
 * A diferencia de una API REST, GraphQL no tiene un endpoint por acción — hay
 * UNA sola URL, y lo que cambia entre "login", "registrarUsuario", etc. es el
 * contenido del [GraphQLRequest] que se manda (el campo `query`).
 *
 * Base URL: http://10.0.2.2:4000/  (el gateway, visto desde el EMULADOR de
 * Android Studio — 10.0.2.2 apunta a "localhost" de la máquina host).
 *
 * Todas las funciones son `suspend` para llamarse desde corrutinas (mismo estilo
 * que GistService).
 */
interface AuthService {
    /**
     * Envía cualquier query/mutation de GraphQL al gateway.
     *
     * @return [GraphQLResponse] con `data` (si salió bien) o `errors` (si no).
     *   A diferencia de REST, GraphQL casi siempre responde HTTP 200 incluso
     *   cuando la operación falla — el error viene DENTRO del body, en `errors`,
     *   no como una excepción HTTP. Por eso [GraphQLResponse.errors] se revisa
     *   a mano en el ViewModel, en vez de esperar un `HttpException`.
     */
    @POST(".")
    suspend fun graphql(@Body body: GraphQLRequest): GraphQLResponse<LoginData>
}

/**
 * Cliente Retrofit para [AuthService]. Timeouts moderados (30 s): a diferencia
 * del backend anterior en Render (con cold start de 30-50s), este backend corre
 * localmente vía Docker/Node, así que responde casi de inmediato una vez que
 * los servicios están arriba.
 */
object AuthRetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:4000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val authService: AuthService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthService::class.java)
}