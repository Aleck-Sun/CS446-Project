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
import com.example.cs446.ui.pages.main.pets.FamilyScreen
import com.example.cs446.ui.pages.main.pets.PetsScreen
import com.example.cs446.ui.pages.main.pets.LogsScreen
import com.example.cs446.ui.pages.main.profile.ProfileScreen
import com.example.cs446.view.social.FeedViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigator(
    feedViewModel: FeedViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentDestination = navBackStackEntry?.destination?.route?.let { route ->
        // Handle routes with parameters
        val baseRoute = route.split("/")[0]
        MainActivityDestination.entries.find { it.name.equals(baseRoute, ignoreCase = true) }
    } ?: MainActivityDestination.Pets

    val navigateTo: (MainActivityDestination, String?) -> Unit = { destination, pathParam ->
        if (destination != currentDestination) {
            val route = if (!pathParam.isNullOrEmpty()) {
                "${destination.name.lowercase()}/$pathParam"
            } else {
                destination.name.lowercase()
            }
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentDestination = currentDestination,
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
                PetsScreen(onNavigate = navigateTo)
            }
            composable(MainActivityDestination.Feed.name.lowercase()) {
                FeedScreen(onNavigate = navigateTo, feedViewModel)
            }
            composable(MainActivityDestination.Profile.name.lowercase()) {
                ProfileScreen(onNavigate = navigateTo)
            }
            composable( "${MainActivityDestination.Logs.name.toLowerCase()}/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                LogsScreen(
                    petId = petId,
                    onNavigate = navigateTo
                )
            }
            composable(MainActivityDestination.Family.name.lowercase()) {
                FamilyScreen(onNavigate = navigateTo)
            }
        }
    }
}
