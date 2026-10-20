package com.ulsa.deportes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = UlsaNavyDarkTheme,
    onPrimary = Color.Black,
    primaryContainer = UlsaNavyContainerDark,
    onPrimaryContainer = Color.White,
    secondary = UlsaGold,
    onSecondary = Color.Black,
    tertiary = UlsaNavyLight,
    background = UlsaBackgroundDark,
    onBackground = UlsaTextPrimaryDark,
    surface = UlsaSurfaceDark,
    onSurface = UlsaTextPrimaryDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = UlsaTextSecondaryDark,
    error = UlsaRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = UlsaNavy,
    onPrimary = Color.White,
    primaryContainer = UlsaNavyLight.copy(alpha = 0.15f),
    onPrimaryContainer = UlsaNavyDark,
    secondary = UlsaGold,
    onSecondary = Color.Black,
    tertiary = UlsaNavyLight,
    background = UlsaBackgroundLight,
    onBackground = UlsaTextPrimaryLight,
    surface = UlsaSurfaceLight,
    onSurface = UlsaTextPrimaryLight,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = UlsaTextSecondaryLight,
    error = UlsaRed,
    onError = Color.White
)

/**
 * Tema Institucional ULSA Deportes.
 *
 * @param dynamicColor Se establece por defecto en `false` para preservar
 * la identidad visual institucional ULSA en Android 12+.
 */
@Composable
fun UlsaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
