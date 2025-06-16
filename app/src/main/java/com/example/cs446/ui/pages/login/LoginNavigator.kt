package com.example.cs446.ui.pages.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs446.view.security.SecurityViewModel

@Composable
fun LoginNavigator(
    onLoggedIn: () -> Unit = {},
    viewModel: SecurityViewModel = SecurityViewModel()
) {
    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = LoginActivityDestination.Login.name.lowercase()) {
        composable(LoginActivityDestination.Register.name.lowercase()) {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(LoginActivityDestination.Login.name.lowercase())
                },
                onRegistered = onLoggedIn,
                modifier = Modifier
            )
        }
        composable(LoginActivityDestination.Login.name.lowercase()) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(LoginActivityDestination.Register.name.lowercase())
                },
                onLoggedIn = onLoggedIn,
                modifier = Modifier
            )
        }
    }
}