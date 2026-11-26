package com.ulsa.deportes

import com.ulsa.deportes.BuildConfig

/**
 * Configuración centralizada de URLs de la API para desarrollo local y producción en la nube.
 * Utiliza BuildConfig con manejo seguro para inyectar dinámicamente las direcciones según el entorno.
 */
object ApiConfig {
    private const val DEV_GATEWAY_URL = "http://10.0.2.2:4000/"
    private const val PROD_GATEWAY_URL = "https://your-production-gateway-url.com/" // TODO: Configurar URL definitiva de producción

    private const val DEV_HOME_URL = "https://androidbasics-auth-api.onrender.com/"
    private const val PROD_HOME_URL = "https://your-production-home-api-url.com/" // TODO: Configurar URL definitiva de producción

    /**
     * Indicador opcional para forzar modo producción.
     */
    var isProduction: Boolean = false

    val gatewayUrl: String
        get() {
            if (isProduction) return PROD_GATEWAY_URL
            return try {
                if (BuildConfig.DEBUG) DEV_GATEWAY_URL else PROD_GATEWAY_URL
            } catch (_: Exception) {
                DEV_GATEWAY_URL
            }
        }

    val homeUrl: String
        get() {
            if (isProduction) return PROD_HOME_URL
            return try {
                if (BuildConfig.DEBUG) DEV_HOME_URL else PROD_HOME_URL
            } catch (_: Exception) {
                DEV_HOME_URL
            }
        }
}
