package com.ulsa.deportes.ui.navigation

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ulsa.deportes.R
import com.ulsa.deportes.common.preferences.AppPreferences
import com.ulsa.deportes.ui.auth.data.SessionPreferences
import com.ulsa.deportes.ui.auth.domain.AuthRepository
import com.ulsa.deportes.ui.auth.domain.AuthRepositoryImpl
import com.ulsa.deportes.ui.auth.view.LoginScreenView
import com.ulsa.deportes.ui.auth.view.RegisterScreenView
import com.ulsa.deportes.ui.auth.viewmodel.LoginViewModel
import com.ulsa.deportes.ui.auth.viewmodel.LogoutViewModel
import com.ulsa.deportes.ui.auth.viewmodel.RegisterViewModel
import com.ulsa.deportes.ui.homeSection.homeHome.view.HomeHomeview
import com.ulsa.deportes.ui.matchesSection.matchesHome.view.matchesHomeView
import com.ulsa.deportes.ui.newsSection.newsHome.view.NewsHomeView
import com.ulsa.deportes.ui.onboarding.data.OnboardingPreferences
import com.ulsa.deportes.ui.onboarding.view.OnboardingScreenView
import com.ulsa.deportes.ui.onboarding.viewmodel.OnboardingViewModel
import com.ulsa.deportes.ui.profileSection.profileHome.view.profileHomeView
import com.ulsa.deportes.ui.qrSection.qrHome.view.QrScreenView
import com.ulsa.deportes.ui.qrSection.qrHome.view.RegistrationScreenView
import com.ulsa.deportes.ui.details.view.*
import com.ulsa.deportes.ui.teamsSection.APIRequest.view.ApiRequestView
import com.ulsa.deportes.ui.teamsSection.teamsHome.view.teamsHomeView

/** Ruta interna (no es tab) para la pantalla de creación de QR. */
private const val QR_ROUTE = "qr_screen"
/** Ruta interna para la pantalla de registro a eventos. */
private const val REGISTRATION_ROUTE = "registration_screen/{eventId}"
/** Ruta para la pantalla de registro de usuario. */
private const val REGISTER_ROUTE = "register"

/**
 * Sealed class defining all bottom-tab routes with their metadata.
 */
sealed class AppRoute(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    object Onboarding : AppRoute("onboarding", R.string.nav_onboarding, Icons.Filled.Start)
    object Login : AppRoute("login", R.string.nav_login, Icons.Filled.Person)
    object APIRequest : AppRoute("api_request", R.string.nav_api, Icons.Filled.Api)
    object TeamsSection : AppRoute("team_section", R.string.nav_teams, Icons.Filled.SportsSoccer)
    object HomeHome : AppRoute("home_home", R.string.nav_home, Icons.Filled.Home)
    object NewsSection : AppRoute("news_section", R.string.nav_news, Icons.Filled.Newspaper)
    object MatchesSection : AppRoute("matches_section", R.string.nav_matches, Icons.Filled.CalendarMonth)
    object ProfileSection : AppRoute("profile_section", R.string.nav_profile, Icons.Filled.Person)
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
    
    // Create AuthRepository once
    val authRepository: AuthRepository = remember { AuthRepositoryImpl(application) }
    
    // Logic to decide start destination based on onboarding status AND session validity
    val startDestination = remember {
        val appPrefs = AppPreferences(context)
        val onboardingPrefs = OnboardingPreferences(appPrefs)
        val sessionPrefs = SessionPreferences(context)
        
        if (!onboardingPrefs.isOnboardingCompleted()) {
            AppRoute.Onboarding.route
        } else if (sessionPrefs.isSessionValid()) {
            "tabs"
        } else {
            AppRoute.Login.route
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
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(application, authRepository)
            )
            LoginScreenView(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("tabs") {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(REGISTER_ROUTE)
                }
            )
        }

        composable(REGISTER_ROUTE) {
            val registerViewModel: RegisterViewModel = viewModel(
                factory = RegisterViewModel.Factory(application, authRepository)
            )
            RegisterScreenView(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate("tabs") {
                        popUpTo(REGISTER_ROUTE) { inclusive = true }
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
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
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes), fontSize = 10.sp) }
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
                    onNavigateToTeam = { teamName ->
                        navController.navigate("team_detail/${Uri.encode(teamName)}")
                    },
                    onNavigateToFirstApi = onNavigateToFirstApi
                )
            }
            composable(AppRoute.HomeHome.route) {
                HomeHomeview(
                    onNavigateToMatch = { matchId -> navController.navigate("match_detail/$matchId") },
                    onNavigateToNews = { newsId -> navController.navigate("news_detail/$newsId") },
                    onNavigateToEvent = { eventId -> navController.navigate("event_detail/$eventId") }
                )
            }
            composable(AppRoute.NewsSection.route) { NewsHomeView() }
            composable(AppRoute.MatchesSection.route) {
                matchesHomeView(
                    onNavigateToMatch = { matchId -> navController.navigate("match_detail/$matchId") }
                )
            }
            composable(AppRoute.ProfileSection.route) {
                profileHomeView(
                    onLogout = onLogout,
                    onNavigateToQr = { navController.navigate(QR_ROUTE) },
                    onRegisterToEvent = { event ->
                        navController.navigate("registration_screen/${event.eventId}")
                    }
                )
            }
            composable(QR_ROUTE) {
                QrScreenView(onBack = { navController.popBackStack() })
            }
            composable(
                route = REGISTRATION_ROUTE,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                val context = LocalContext.current
                val eventPreferences = remember { com.ulsa.deportes.ui.qrSection.qrHome.data.EventPreferences(context) }
                val event = eventPreferences.getEventList().find { it.eventId == eventId }
                if (event != null) {
                    RegistrationScreenView(event = event, onBack = { navController.popBackStack() })
                } else {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.nav_event_not_found),
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // F06: Detalle de Partido, Noticia, Evento y Equipo
            composable(
                route = "match_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                MatchDetailView(onBack = { navController.popBackStack() })
            }
            composable(
                route = "news_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                NewsDetailView(onBack = { navController.popBackStack() })
            }
            composable(
                route = "event_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                EventDetailView(onBack = { navController.popBackStack() })
            }
            composable(
                route = "team_detail/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) {
                TeamDetailView(onBack = { navController.popBackStack() })
            }
        }
    }
}
