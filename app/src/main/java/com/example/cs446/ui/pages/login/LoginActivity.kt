package com.example.cs446.ui.pages.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs446.backend.data.result.AuthResult
import com.example.cs446.ui.pages.MainActivity
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.view.security.SecurityViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fun onLogin() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val viewModel = SecurityViewModel()

        setContent {
            LoginNavigator(
                ::onLogin,
                viewModel
            )
        }
    }
}

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
                onNavigateToLogin = { navController.navigate("register") },
                onRegistered = onLoggedIn, // automatically log in after registration
                modifier = Modifier
            )
        }
    }
}
