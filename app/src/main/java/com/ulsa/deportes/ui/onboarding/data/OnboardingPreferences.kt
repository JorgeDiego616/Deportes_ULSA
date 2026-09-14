package com.ulsa.deportes.ui.onboarding.data

import com.ulsa.deportes.common.preferences.AppPreferences

class OnboardingPreferences(private val appPreferences: AppPreferences) {

    companion object {
        private const val ONBOARDING_COMPLETED_KEY = "onboarding_completed"
    }

    fun isOnboardingCompleted(): Boolean {
        return appPreferences.getBoolean(ONBOARDING_COMPLETED_KEY, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        appPreferences.putBoolean(ONBOARDING_COMPLETED_KEY, completed)
    }
}
