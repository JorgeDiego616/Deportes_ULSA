package com.ulsa.deportes.ui.onboarding.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ulsa.deportes.R
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.onboarding.data.OnboardingPreferences
import com.ulsa.deportes.ui.onboarding.model.OnboardingPage

class OnboardingViewModel(private val onboardingPreferences: OnboardingPreferences) : ViewModel() {

    val pages = listOf(
        OnboardingPage(
            title = "All Your Teams, One Place",
            description = "Sigue los resultados, estadísticas y noticias de tus equipos favoritos en tiempo real.",
            imageRes = R.drawable.onboarding_1
        ),
        OnboardingPage(
            title = "Statistics that matter",
            description = "Analiza datos clave, alineaciones y rendimientos de forma clara y rápida.",
            imageRes = R.drawable.onboarding_2
        ),
        OnboardingPage(
            title = "Don't miss any games",
            description = "Recibe alertas al instante y vive la emoción de cada partido minuto a minuto.",
            imageRes = R.drawable.onboarding_3
        )
    )

    fun completeOnboarding() {
        onboardingPreferences.setOnboardingCompleted(true)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                val appPreferences = AppPreferences(application)
                val onboardingPreferences = OnboardingPreferences(appPreferences)
                @Suppress("UNCHECKED_CAST")
                return OnboardingViewModel(onboardingPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
