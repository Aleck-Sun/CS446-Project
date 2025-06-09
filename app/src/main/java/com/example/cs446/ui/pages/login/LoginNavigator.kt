package com.example.cs446.ui.pages.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs446.backend.data.result.AuthResult
import com.example.cs446.view.security.SecurityViewModel

@Composable
fun LoginNavigator(
    onLoggedIn: () -> Unit = {},
    viewModel: SecurityViewModel = SecurityViewModel()
) {
    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoggedIn = onLoggedIn,
                modifier = Modifier
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.navigate("login") },
                onRegistered = onLoggedIn, // automatically log in after registration
                modifier = Modifier
            )
        }
    }
}