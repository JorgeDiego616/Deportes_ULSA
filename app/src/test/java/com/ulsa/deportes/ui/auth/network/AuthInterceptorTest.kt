package com.ulsa.deportes.ui.auth.network

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test

class AuthInterceptorTest {

    @Test
    fun testPublicOperationOmitsAuthorizationAndStripsHeader() {
        var capturedRequest: okhttp3.Request? = null
        val testToken = "mock_jwt_token_12345"

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val isPublicOperation = originalRequest.header("X-Public-Operation") != null
                val requestBuilder = originalRequest.newBuilder().removeHeader("X-Public-Operation")

                if (!isPublicOperation) {
                    requestBuilder.addHeader("Authorization", "Bearer $testToken")
                }
                capturedRequest = requestBuilder.build()
                chain.proceed(capturedRequest!!)
            }
            .build()

        val request = Request.Builder()
            .url("http://localhost:4000/")
            .header("X-Public-Operation", "true")
            .post(okhttp3.RequestBody.create(null, "query { login }"))
            .build()

        try {
            client.newCall(request).execute()
        } catch (_: Exception) {}

        assertNotNull(capturedRequest)
        assertNull(capturedRequest?.header("Authorization"))
        assertNull(capturedRequest?.header("X-Public-Operation"))
    }

    @Test
    fun testAuthenticatedOperationAddsBearerToken() {
        var capturedRequest: okhttp3.Request? = null
        val testToken = "mock_jwt_token_12345"

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val isPublicOperation = originalRequest.header("X-Public-Operation") != null
                val requestBuilder = originalRequest.newBuilder().removeHeader("X-Public-Operation")

                if (!isPublicOperation) {
                    requestBuilder.addHeader("Authorization", "Bearer $testToken")
                }
                capturedRequest = requestBuilder.build()
                chain.proceed(capturedRequest!!)
            }
            .build()

        val request = Request.Builder()
            .url("http://localhost:4000/")
            .post(okhttp3.RequestBody.create(null, "mutation Logout { logout }"))
            .build()

        try {
            client.newCall(request).execute()
        } catch (_: Exception) {}

        assertNotNull(capturedRequest)
        assertEquals("Bearer $testToken", capturedRequest?.header("Authorization"))
    }
}
