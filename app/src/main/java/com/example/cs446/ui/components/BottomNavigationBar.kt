package com.example.cs446.ui.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import com.example.cs446.ui.pages.main.MainActivityDestination

@Composable
fun BottomNavigationBar(
    currentDestination: MainActivityDestination?,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination == MainActivityDestination.Pets,
            onClick = { onNavigate(MainActivityDestination.Pets, null) },
            icon = { Icon(Icons.Default.Face, contentDescription = "Pets") },
            label = { Text("Pets") }
        )
        NavigationBarItem(
            selected = currentDestination == MainActivityDestination.Feed,
            onClick = { onNavigate(MainActivityDestination.Feed, null) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
            label = { Text("Feed") }
        )
        NavigationBarItem(
            selected = currentDestination == MainActivityDestination.Profile,
            onClick = { onNavigate(MainActivityDestination.Profile, null) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
