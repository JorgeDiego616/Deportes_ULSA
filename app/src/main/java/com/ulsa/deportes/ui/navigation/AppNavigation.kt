package com.ulsa.deportes.ui.navigation

import com.ulsa.deportes.ui.teamsSection.APIRequest.view.ApiRequestView
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Newspaper

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.ulsa.deportes.ui.homeSection.homeHome.view.HomeHomeview
import com.ulsa.deportes.ui.teamsSection.teamsHome.view.teamsHomeView
import com.ulsa.deportes.ui.matchesSection.matchesHome.view.matchesHomeView
import com.ulsa.deportes.ui.profileSection.profileHome.view.profileHomeView
import com.ulsa.deportes.ui.login.view.LoginView
import com.ulsa.deportes.ui.newsSection.newsHome.view.NewsHomeView

/**
 * Sealed class defining all bottom-tab routes with their metadata.
 */
//sealed class para los botones y que funcionen en la app
sealed class AppRoute(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object Login : AppRoute("login")
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only if not in login screen
    val showBottomBar = currentRoute != AppRoute.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
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
                            icon = { Icon(tab.icon!!, contentDescription = tab.label) },
                            label = { Text(tab.label!!, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Login.route) {
                LoginView(onLoginSuccess = {
                    navController.navigate(AppRoute.TeamsSection.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                })
            }
            
            composable(AppRoute.TeamsSection.route) {
                teamsHomeView(
                    onNavigateToFirstApi = { navController.navigate("api_request") }
                )
            }
            composable(AppRoute.HomeHome.route) { HomeHomeview() }
            composable(AppRoute.NewsSection.route) { NewsHomeView() }
            composable(AppRoute.MatchesSection.route) { matchesHomeView() }
            composable(AppRoute.ProfileSection.route) { profileHomeView() }

            composable("api_request") {
                ApiRequestView(onBack = { navController.popBackStack() })
            }
        }
    }
}