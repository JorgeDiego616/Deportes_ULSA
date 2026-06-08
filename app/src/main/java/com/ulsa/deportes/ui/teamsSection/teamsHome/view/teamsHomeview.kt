package com.ulsa.deportes.ui.teamsSection.teamsHome.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.teamsSection.teamsHome.viewmodel.teamsHomeviewmodel

@Composable
fun teamsHomeView(
    onNavigateToFirstApi: () -> Unit = {},   // recibe la acción de navegar
    homeViewModel: teamsHomeviewmodel = viewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onNavigateToFirstApi) {
            Text("Teams Data")
        }
    }
}