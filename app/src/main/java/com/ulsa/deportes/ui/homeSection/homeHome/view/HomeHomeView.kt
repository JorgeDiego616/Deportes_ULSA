package com.ulsa.deportes.ui.homeSection.homeHome.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.homeSection.homeHome.viewmodel.HomeHomeviewmodel

/**
 * Screen composable for the First Partial PDM1 section.
 * Displays centered placeholder text driven by [HomeFirstPartialPDM1ViewModel].
 * @param homeViewModel ViewModel providing state for this screen.
 */
@Composable
fun HomeHomeview(
    homeViewModel: HomeHomeviewmodel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "⭐ Welcome to ULSA Sports ⭐")
    }
}