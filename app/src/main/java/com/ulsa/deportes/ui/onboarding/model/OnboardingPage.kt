package com.ulsa.deportes.ui.onboarding.model

import androidx.annotation.DrawableRes

data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int
)
