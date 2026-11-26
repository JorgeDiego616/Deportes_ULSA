package com.ulsa.deportes.ui.auth.network

import android.util.Log
import com.ulsa.deportes.ApiConfig
import com.ulsa.deportes.ui.auth.model.GraphQLRequest
import com.ulsa.deportes.ui.auth.model.GraphQLResponse
import com.ulsa.deportes.ui.auth.model.LoginData
import com.ulsa.deportes.ui.auth.model.LogoutData
import com.ulsa.deportes.ui.auth.model.RegisterData
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface AuthService {
    @Headers("Content-Type: application/json", "Accept: application/json", "X-Public-Operation: true")
    @POST(".")
    suspend fun graphql(@Body body: GraphQLRequest): GraphQLResponse<LoginData>

    @Headers("Content-Type: application/json", "Accept: application/json", "X-Public-Operation: true")
    @POST(".")
    suspend fun graphqlRegister(@Body body: GraphQLRequest): GraphQLResponse<RegisterData>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(".")
    suspend fun logout(@Body body: GraphQLRequest): GraphQLResponse<LogoutData>
}

private class AuthLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // Nunca registrar tokens, contraseñas ni cuerpos de autenticación en Logcat
        Log.d("AuthNetwork", "--> Sending ${request.method} to ${request.url}")
        val response = chain.proceed(request)
        Log.d("AuthNetwork", "<-- Received response ${response.code} for ${request.url}")
        return response
    }
}

object AuthRetrofitClient {
    var tokenProvider: (() -> String?)? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            // Detectar operaciones públicas explícitamente mediante encabezado interno X-Public-Operation
            val isPublicOperation = originalRequest.header("X-Public-Operation") != null

            // Retiramos el encabezado interno antes de enviar la petición al servidor
            val requestBuilder = originalRequest.newBuilder()
                .removeHeader("X-Public-Operation")

            if (!isPublicOperation) {
                val token = tokenProvider?.invoke()
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
            }

            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(AuthLoggingInterceptor())
        .build()

    val authService: AuthService = Retrofit.Builder()
        .baseUrl(ApiConfig.gatewayUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthService::class.java)
}
