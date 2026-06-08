package com.ulsa.deportes.ui.matchesSection.matchesHome.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.matchesHomeViewModel


@Composable
fun matchesHomeView(
    homeViewModel: matchesHomeViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "There are no games today 😢")
    }
}