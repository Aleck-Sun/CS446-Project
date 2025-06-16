package com.example.cs446.ui.pages.main

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
import com.example.cs446.ui.pages.main.feed.FeedScreen
import com.example.cs446.ui.pages.main.profile.ProfileScreen

@Composable
fun MainNavigator() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentDestination = navBackStackEntry?.destination?.route?.let { route ->
        MainActivityDestination.entries.find { it.name.equals(route, ignoreCase = true) }
    } ?: MainActivityDestination.Pets

    val navigateTo: (MainActivityDestination) -> Unit = { destination ->
        if (destination != currentDestination) {
            navController.navigate(destination.name.lowercase()) {
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
                FeedScreen(onNavigate = navigateTo)
            }
            composable(MainActivityDestination.Profile.name.lowercase()) {
                ProfileScreen(onNavigate = navigateTo)
            }
            composable(MainActivityDestination.Logs.name.lowercase()) {
                LogsScreen(onNavigate = navigateTo)
            }
            composable(MainActivityDestination.Family.name.lowercase()) {
                FamilyScreen(onNavigate = navigateTo)
            }
        }
    }
}
