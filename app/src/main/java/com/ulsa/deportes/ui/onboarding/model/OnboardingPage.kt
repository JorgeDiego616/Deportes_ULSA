package com.ulsa.deportes.ui.onboarding.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingPage(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val imageRes: Int
)
