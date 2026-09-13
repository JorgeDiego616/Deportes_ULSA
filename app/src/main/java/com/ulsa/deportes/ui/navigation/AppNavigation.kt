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
import com.ulsa.deportes.ui.newsSection.newsHome.view.NewsHomeView
import com.ulsa.deportes.ui.auth.view.LoginScreenView
import com.ulsa.deportes.ui.auth.viewmodel.LogoutViewModel

/**
 * Sealed class defining all bottom-tab routes with their metadata.
 */
sealed class AppRoute(val route: String, val label: String, val icon: ImageVector) {
    object Login : AppRoute("login", "Login", Icons.Filled.Person)
    object APIRequest : AppRoute("api_request", "API", Icons.Filled.Api)
    object TeamsSection : AppRoute("team_section", "Teams", Icons.Filled.SportsSoccer)
    object HomeHome : AppRoute("home_home", "Home", Icons.Filled.Home)
    object NewsSection : AppRoute("news_section", "News", Icons.Filled.Newspaper)
    object MatchesSection : AppRoute("matches_section", "Matches", Icons.Filled.CalendarMonth)
    object ProfileSection : AppRoute("profile_section", "Profile", Icons.Filled.Person)
}

/** Ordered list of all tabs shown in the bottom bar. */
// CAMBIO: este comentario cambió — ahora aclara que TABS solo lo usa
// TabsScaffold. Antes se usaba TAMBIÉN en el NavigationBar de AppNavigation(),
// que ya no existe (ver más abajo).
private val TABS = listOf(
    AppRoute.TeamsSection,
    AppRoute.HomeHome,
    AppRoute.NewsSection,
    AppRoute.MatchesSection,
    AppRoute.ProfileSection,
)

/**
 * Grafo de navegación de nivel raíz. Solo decide entre Login o el conjunto de
 * tabs (TabsScaffold) — YA NO tiene su propio Scaffold/NavigationBar, para
 * evitar la duplicación de barras que causaba que el logout no funcionara.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ELIMINADO: ya no se leen navBackStackEntry / currentRoute / showBottomBar
    // aquí. Eran solo para decidir si mostrar la barra de navegación EXTERNA,
    // que también se eliminó (ver más abajo). TabsScaffold ya maneja su propia
    // barra internamente con su propio currentRoute.
    //
    // val navBackStackEntry by navController.currentBackStackEntryAsState()
    // val currentRoute = navBackStackEntry?.destination?.route
    // val showBottomBar = currentRoute != AppRoute.Login.route

    // ELIMINADO: todo el Scaffold + NavigationBar que estaba aquí. Este era
    // el causante del bug: al tocar un tab en ESTA barra, se navegaba a rutas
    // registradas en ESTE NavHost (ver abajo qué también se quitó de aquí),
    // que llamaban a profileHomeView() SIN onLogout, dejando el botón de
    // "Cerrar sesión" sin conectar a nada (usaba el valor default onLogout = {}).
    //
    // Scaffold(
    //     bottomBar = {
    //         if (showBottomBar) {
    //             NavigationBar {
    //                 TABS.forEach { tab -> ... }
    //             }
    //         }
    //     }
    // ) { innerPadding -> ... }

    // CAMBIO: el NavHost ya no vive dentro de un Scaffold externo, y ya no
    // recibe modifier = Modifier.padding(innerPadding) (ese padding solo tenía
    // sentido para compensar la barra externa que ya no existe).
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route
    ) {
        // NUEVO: antes este composable("login") NO EXISTÍA en el archivo — por
        // eso crasheaba con "navigation destination login is not a direct
        // child of this NavGraph" al arrancar. Aquí se agrega LoginScreenView.
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
                        // CAMBIO: antes decía navController.navigate("login")
                        // con el string suelto; ahora usa la constante
                        // AppRoute.Login.route (mismo valor, más seguro ante
                        // futuros cambios de nombre de ruta).
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

        // ELIMINADO: estas 5 rutas vivían aquí, DUPLICANDO las que ya existen
        // (correctamente, con onLogout conectado) dentro del NavHost interno
        // de TabsScaffold. Eran alcanzables solo a través de la barra externa
        // ya eliminada, y su versión de profileHomeView() (sin onLogout) era
        // la causa exacta del bug de logout.
        //
        // composable(AppRoute.TeamsSection.route) {
        //     teamsHomeView(onNavigateToFirstApi = { navController.navigate("api_request") })
        // }
        // composable(AppRoute.HomeHome.route) { HomeHomeview() }
        // composable(AppRoute.NewsSection.route) { NewsHomeView() }
        // composable(AppRoute.MatchesSection.route) { matchesHomeView() }
        // composable(AppRoute.ProfileSection.route) { profileHomeView() }

        composable("api_request") {
            ApiRequestView(onBack = { navController.popBackStack() })
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
    // SIN CAMBIOS: esta función ya estaba bien armada — es la única fuente de
    // verdad para la barra de tabs y sus rutas. El bug nunca estuvo aquí.
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
                // CONFIRMADO SIN CAMBIOS: esta es la única versión correcta,
                // la que sí conecta onLogout con el botón real.
                profileHomeView(onLogout = onLogout)
            }
        }
    }
}