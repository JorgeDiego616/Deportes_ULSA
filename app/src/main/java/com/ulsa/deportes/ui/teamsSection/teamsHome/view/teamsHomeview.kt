package com.ulsa.deportes.ui.teamsSection.teamsHome.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.teamsSection.APIRequest.view.TeamsListContent
import com.ulsa.deportes.ui.teamsSection.teamsHome.viewmodel.teamsHomeviewmodel

@Composable
fun teamsHomeView(
    @Suppress("UNUSED_PARAMETER") onNavigateToFirstApi: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") homeViewModel: teamsHomeviewmodel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TeamsListContent()
    }
}
