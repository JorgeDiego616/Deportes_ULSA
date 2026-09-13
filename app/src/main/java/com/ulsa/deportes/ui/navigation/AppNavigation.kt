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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.ulsa.deportes.ui.homeSection.homeHome.view.HomeHomeview
import com.ulsa.deportes.ui.teamsSection.teamsHome.view.teamsHomeView
import com.ulsa.deportes.ui.matchesSection.matchesHome.view.matchesHomeView
import com.ulsa.deportes.ui.profileSection.profileHome.view.profileHomeView
// import com.ulsa.deportes.ui.login.view.LoginView
import com.ulsa.deportes.ui.newsSection.newsHome.view.NewsHomeView
import com.ulsa.deportes.ui.auth.view.LoginScreenView
import com.ulsa.deportes.ui.auth.viewmodel.LogoutViewModel

/**
 * Sealed class defining all bottom-tab routes with their metadata.
 */
//sealed class para los botones y que funcionen en la app
sealed class AppRoute(val route: String, val label: String, val icon: ImageVector) {
    object Login : AppRoute("login","Login", Icons.Filled.Person)
    object APIRequest : AppRoute("api_request", "API", Icons.Filled.Api)
    object TeamsSection : AppRoute("team_section", "Teams", Icons.Filled.SportsSoccer)
    object HomeHome : AppRoute("home_home", "Home", Icons.Filled.Home)
    object NewsSection : AppRoute("news_section", "News", Icons.Filled.Newspaper)
    object MatchesSection : AppRoute("matches_section", "Matches", Icons.Filled.CalendarMonth)
    object ProfileSection : AppRoute("profile_section", "Profile", Icons.Filled.Person)
}

/** Ordered list of all tabs shown in the bottom bar. */
private val TABS = listOf(
    AppRoute.TeamsSection, //ThirdPartialIDS2
    AppRoute.HomeHome, //FirstPartialPDM1
    AppRoute.NewsSection, //SecondPartialPDM1
    AppRoute.MatchesSection, //ThirdPartialPDM1
    AppRoute.ProfileSection, //PersonalInformation
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
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 10.sp) }
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
            // Aqui se pone lo del onboarding
//            composable("onboarding") {
//                OnboardingView(
//                    onFinishOnboarding = {
//                        rootNavController.navigate("login") {
//                            popUpTo("onboarding") { inclusive = true }
//                        }
//                    }
//                )
//            }

            composable("tabs") {
                val logoutViewModel: LogoutViewModel = viewModel()
                TabsScaffold(
                    onLogout = {
                        logoutViewModel.logout {
                            navController.navigate("login") {
                                popUpTo("tabs") { inclusive = true }
                            }
                        }
                    },
                    onNavigateToFirstApi = { navController.navigate("first_api_request") },
                    onNavigateToSharedPreferencesExample = { navController.navigate("shared_preferences_example") },
                    onNavigateToJetPackComposeExample = { navController.navigate("jetpack_compose_examples") },
                    onNavigateTodetailColumn = { navController.navigate("detail_Column") }
                )
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


@Composable
private fun TabsScaffold(
    onLogout: () -> Unit,
    onNavigateToFirstApi: () -> Unit, /* este se cambio al original del profe */
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
                HomeHomeview(
                    // aqui se hacen las rutas de los botones en la funcion de HomeHomeview o en el repo del profe es HomeFirstPartialPDM1View
                    // Si llegamos a poner botones, la escrutura de abajo sirve para decir a donde dirige cada boton.
//                    onNavigateToSharedPreferencesExample = onNavigateToSharedPreferencesExample,
//                    onNavigateToJetPackComposeExample = onNavigateToJetPackComposeExample,
//                    onNavigateTodetailColumn = onNavigateTodetailColumn
                )
            }
            composable(AppRoute.NewsSection.route) { NewsHomeView() }
            composable(AppRoute.MatchesSection.route) { matchesHomeView() }
            composable(AppRoute.ProfileSection.route) {
                profileHomeView(onLogout = onLogout)
            }
        }
    }
}