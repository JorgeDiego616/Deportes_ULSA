package com.ulsa.deportes.ui.onboarding.viewmodel

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringArrayResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ulsa.deportes.R
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.onboarding.data.OnboardingPreferences
import com.ulsa.deportes.ui.onboarding.model.OnboardingPage

class OnboardingViewModel(private val onboardingPreferences: OnboardingPreferences) : ViewModel() {

    val pages = listOf(
        OnboardingPage(
            title = R.string.onboarding_title_1,
            description = R.string.onboarding_desc_1,
            imageRes = R.drawable.onboarding_1
        ),
        OnboardingPage(
            title = R.string.onboarding_title_2,
            description = R.string.onboarding_desc_2,
            imageRes = R.drawable.onboarding_2
        ),
        OnboardingPage(
            title = R.string.onboarding_title_3,
            description = R.string.onboarding_desc_3,
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
