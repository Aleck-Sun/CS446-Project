package com.example.cs446.ui.pages.main

import FeedScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cs446.ui.components.BottomNavigationBar
import com.example.cs446.ui.pages.main.pets.LogsScreen
import com.example.cs446.ui.pages.main.pets.PermissionsScreen
import com.example.cs446.ui.pages.main.pets.PetsScreen
import com.example.cs446.ui.pages.main.profile.ProfileScreen
import com.example.cs446.view.pets.HandlerViewModel
import com.example.cs446.view.pets.PetsViewModel
import com.example.cs446.view.social.FeedViewModel
import com.example.cs446.view.social.ProfileViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigator(
    petsViewModel: PetsViewModel,
    feedViewModel: FeedViewModel,
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit = {},
    handlerViewModel: HandlerViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentMainDestination = currentDestination?.route?.let { route ->
        val baseRoute = route.split("/")[0]
        MainActivityDestination.entries.find { it.name.equals(baseRoute, ignoreCase = true) }
    } ?: MainActivityDestination.Pets

    val navigateTo: (MainActivityDestination, String?) -> Unit = { destination, param ->
        val route = if (param != null) "${destination.name.lowercase()}/$param"
        else destination.name.lowercase()

        val currentFullRoute = navController.currentDestination?.route
        val currentRoute = currentFullRoute?.split("/")?.firstOrNull() ?: currentFullRoute

        if (route != currentRoute) {
            if (
                currentRoute == MainActivityDestination.Logs.name.lowercase()
                && route == MainActivityDestination.Pets.name.lowercase()
                && param == null) {
                navController.popBackStack()
            } else if (param != null) {
                navController.navigate(route)
            } else {
                navController.navigate(destination.name.lowercase()) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentDestination = currentMainDestination,
                onNavigate = navigateTo
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainActivityDestination.Pets.name.lowercase(),
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainActivityDestination.Pets.name.lowercase()) {
                PetsScreen(onNavigate = navigateTo, viewModel = petsViewModel)
            }
            composable(MainActivityDestination.Feed.name.lowercase()) {
                FeedScreen(onNavigate = navigateTo, viewModel = feedViewModel)
            }
            composable(MainActivityDestination.Profile.name.lowercase()) {
                ProfileScreen(onNavigate = navigateTo, viewModel = profileViewModel)
            }
            composable("${MainActivityDestination.Logs.name.lowercase()}/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                LogsScreen(
                    petId = petId,
                    viewModel = feedViewModel,
                    onNavigate = navigateTo
                )
            }
            composable("${MainActivityDestination.Handlers.name.lowercase()}/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                PermissionsScreen(
                    petId = petId,
                    onNavigate = navigateTo,
                    viewModel = handlerViewModel
                )
            }
        }
    }
}
