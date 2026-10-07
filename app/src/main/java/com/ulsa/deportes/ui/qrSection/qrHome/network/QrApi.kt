package com.ulsa.deportes.ui.qrSection.qrHome.network

import com.ulsa.deportes.ui.qrSection.qrHome.model.CapacityStatus
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrRequest
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Contrato de red del módulo de QR.
 *
 * NOTA: hoy el backend (UlsaHub) NO expone estos endpoints y su gateway habla
 * GraphQL, no REST. Esta interfaz deja el contrato listo para cuando existan.
 * Mientras tanto, [QrDependencies.USE_MOCK] sirve datos simulados.
 */
interface QrApi {
    @POST("qr")
    suspend fun createQr(@Body request: CreateQrRequest): CreateQrResponse

    @GET("events/{eventId}/capacity")
    suspend fun getCapacity(@Path("eventId") eventId: String): CapacityStatus
}

/**
 * Cliente Retrofit real, apuntando al MISMO host/puerto que el login (el gateway
 * de Apollo en http://10.0.2.2:4000/). Adjunta el token de sesión del login como
 * Authorization: Bearer, leyéndolo de [com.ulsa.deportes.ui.auth.data.SessionPreferences].
 */
object QrApiClient {

    private const val BASE_URL = "http://10.0.2.2:4000/"

    fun create(sessionToken: () -> String?): QrApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = sessionToken()
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QrApi::class.java)
    }
}
