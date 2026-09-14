package com.ulsa.deportes.ui.navigation

import android.app.Application
import com.ulsa.deportes.ui.teamsSection.APIRequest.view.ApiRequestView
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Start

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ulsa.deportes.common.preferences.AppPreferences

import com.ulsa.deportes.ui.homeSection.homeHome.view.HomeHomeview
import com.ulsa.deportes.ui.teamsSection.teamsHome.view.teamsHomeView
import com.ulsa.deportes.ui.matchesSection.matchesHome.view.matchesHomeView
import com.ulsa.deportes.ui.profileSection.profileHome.view.profileHomeView
import com.ulsa.deportes.ui.newsSection.newsHome.view.NewsHomeView
import com.ulsa.deportes.ui.auth.view.LoginScreenView
import com.ulsa.deportes.ui.auth.viewmodel.LogoutViewModel
import com.ulsa.deportes.ui.onboarding.data.OnboardingPreferences
import com.ulsa.deportes.ui.onboarding.view.OnboardingScreenView
import com.ulsa.deportes.ui.onboarding.viewmodel.OnboardingViewModel

/**
 * Sealed class defining all bottom-tab routes with their metadata.
 */
sealed class AppRoute(val route: String, val label: String, val icon: ImageVector) {
    object Onboarding : AppRoute("onboarding", "Onboarding", Icons.Filled.Start)
    object Login : AppRoute("login", "Login", Icons.Filled.Person)
    object APIRequest : AppRoute("api_request", "API", Icons.Filled.Api)
    object TeamsSection : AppRoute("team_section", "Teams", Icons.Filled.SportsSoccer)
    object HomeHome : AppRoute("home_home", "Home", Icons.Filled.Home)
    object NewsSection : AppRoute("news_section", "News", Icons.Filled.Newspaper)
    object MatchesSection : AppRoute("matches_section", "Matches", Icons.Filled.CalendarMonth)
    object ProfileSection : AppRoute("profile_section", "Profile", Icons.Filled.Person)
}

/** Ordered list of all tabs shown in the bottom bar. */
private val TABS = listOf(
    AppRoute.TeamsSection,
    AppRoute.HomeHome,
    AppRoute.NewsSection,
    AppRoute.MatchesSection,
    AppRoute.ProfileSection,
)

/**
 * Grafo de navegación de nivel raíz.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    
    // Logic to decide start destination based on onboarding status
    val startDestination = remember {
        val appPrefs = AppPreferences(context)
        val onboardingPrefs = OnboardingPreferences(appPrefs)
        if (onboardingPrefs.isOnboardingCompleted()) {
            AppRoute.Login.route
        } else {
            AppRoute.Onboarding.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoute.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.Factory(application)
            )
            OnboardingScreenView(
                viewModel = onboardingViewModel,
                onFinished = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Login.route) {
            LoginScreenView(
                onLoginSuccess = {
                    navController.navigate("tabs") {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable("tabs") {
            val logoutViewModel: LogoutViewModel = viewModel()
            TabsScaffold(
                onLogout = {
                    logoutViewModel.logout {
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo("tabs") { inclusive = true }
                        }
                    }
                },
                onNavigateToFirstApi = { navController.navigate("api_request") },
                onNavigateToSharedPreferencesExample = { navController.navigate("shared_preferences_example") },
                onNavigateToJetPackComposeExample = { navController.navigate("jetpack_compose_examples") },
                onNavigateTodetailColumn = { navController.navigate("detail_Column") }
            )
        }

        composable("api_request") {
            ApiRequestView(onBack = { navController.popBackStack() })
        }
    }
}


@Composable
private fun TabsScaffold(
    onLogout: () -> Unit,
    onNavigateToFirstApi: () -> Unit,
    onNavigateToSharedPreferencesExample: () -> Unit,
    onNavigateToJetPackComposeExample: () -> Unit,
    onNavigateTodetailColumn: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                TABS.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.TeamsSection.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.TeamsSection.route) {
                teamsHomeView(onNavigateToFirstApi = onNavigateToFirstApi)
            }
            composable(AppRoute.HomeHome.route) {
                HomeHomeview()
            }
            composable(AppRoute.NewsSection.route) { NewsHomeView() }
            composable(AppRoute.MatchesSection.route) { matchesHomeView() }
            composable(AppRoute.ProfileSection.route) {
                profileHomeView(onLogout = onLogout)
            }
        }
    }
}
