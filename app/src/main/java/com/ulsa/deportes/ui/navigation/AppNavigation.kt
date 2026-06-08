package com.ulsa.deportes.ui.navigation

import com.ulsa.deportes.ui.teamsSection.APIRequest.view.ApiRequestView
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.CalendarMonth

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

//import com.example.emptyactivity.ui.firstpartialpdm1.homeFirstPartialPDM1.view.HomeFirstPartialPDM1View

import com.ulsa.deportes.ui.homeSection.homeHome.view.HomeHomeview
import com.ulsa.deportes.ui.teamsSection.teamsHome.view.teamsHomeView
import com.ulsa.deportes.ui.matchesSection.matchesHome.view.matchesHomeView
import com.ulsa.deportes.ui.profileSection.profileHome.view.profileHomeView

//import com.example.emptyactivity.ui.secondpartialpdm1.homeSecondPartialPDM1.view.HomeSecondPartialPDM1View
//import com.example.emptyactivity.ui.thirdpartialids2.homeThirdPartialIDS2.view.HomeThirdPartialIDS2View
//import com.example.emptyactivity.ui.thirdpartialpdm1.homeThirdPartialPDM1.view.HomeThirdPartialPDM1View

/**
 * Sealed class defining all bottom-tab routes with their metadata.
 * @property route Unique route string used by the NavHost.
 * @property label Short label shown beneath the tab icon.
 * @property icon Icon displayed in the NavigationBar item.
 */
sealed class AppRoute(val route: String, val label: String, val icon: ImageVector) {
    // --
    // object FirstApiRequestView : AppRoute("first_api_request", "API", Icons.Filled.School)

    object APIRequest : AppRoute("api_request", "API", Icons.Filled.Api) // ---------

    object TeamsSection : AppRoute("team_section", "Teams", Icons.Filled.SportsSoccer) // ---

    object HomeHome : AppRoute("home_home", "Home", Icons.Filled.Home) // --------

    object MatchesSection : AppRoute("matches_section", "Matches", Icons.Filled.CalendarMonth) // --------

    object ProfileSection : AppRoute("profile_section", "Profile", Icons.Filled.Person) // --------


//    object ThirdPartialIDS2 : AppRoute("third_partial_ids2", "IDS2 P3", Icons.Filled.School)
//    object FirstPartialPDM1 : AppRoute("first_partial_pdm1", "PDM1 P1", Icons.Filled.PhoneAndroid)
//
//    object ThirdPartialPDM1 : AppRoute("third_partial_pdm1", "PDM1 P3", Icons.Filled.Smartphone)
//    object PersonalInformation : AppRoute("personal_information", "About Me", Icons.Filled.Person)
}

/** Ordered list of all tabs shown in the bottom bar. */
private val TABS = listOf(
    AppRoute.TeamsSection,
    AppRoute.HomeHome,
    AppRoute.MatchesSection,
    AppRoute.ProfileSection,
    //AppRoute.PersonalInformation
)

/**
 * Root composable that hosts the Scaffold with a bottom NavigationBar and
 * a NavHost wired to the five main screens of the app.
 */
@Composable
fun AppNavigation() {
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
                teamsHomeView(
                    onNavigateToFirstApi = { navController.navigate("api_request") }
                )
            }
            /*composable(AppRoute.ThirdPartialIDS2.route) { HomeThirdPartialIDS2View() }*/
            composable(AppRoute.HomeHome.route) { HomeHomeview() }
            composable(AppRoute.MatchesSection.route) { matchesHomeView() }
            composable(AppRoute.ProfileSection.route) { profileHomeView() }
            //composable(AppRoute.PersonalInformation.route) { teamsHomeView() }

            composable("api_request") {
                ApiRequestView(onBack = { navController.popBackStack() })
            }
        }
    }
}