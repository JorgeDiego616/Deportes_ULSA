package com.ulsa.deportes.ui.profileSection.profileHome.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.profileSection.profileHome.viewmodel.profileHomeViewModel

@Composable
fun profileHomeView(
    homeViewModel: profileHomeViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🔨 We are working on it 🔨")
    }
}