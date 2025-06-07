package com.example.cs446.ui.pages.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cs446.backend.data.result.AuthResult
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.view.security.SecurityViewModel

@Composable
fun LoginScreen(
    viewModel: SecurityViewModel,
    onNavigateToRegister: () -> Unit,
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authState by viewModel.authState.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthResult.LoginSuccess) {
            onLoggedIn()
        }
    }
    CS446Theme {
        LoginBox(
            onNavigateToRegister = onNavigateToRegister,
            onLogin = { email: String, password: String ->
                viewModel.login(email, password)
            },
            authState = authState,
            modifier = modifier
        )
    }
}

@Composable
fun LoginBox(
    onNavigateToRegister: () -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> },
    authState: AuthResult,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Petfolio",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Please sign in to continue",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = {email=it},
                label = { Text("Email") },
                modifier = Modifier.padding(bottom = 8.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = {password=it},
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(bottom = 8.dp)

            )
            when (authState) {
                is AuthResult.LoginSuccess -> Text(
                    text = "Login successful",
                    color = Color.Green
                )
                is AuthResult.LoginError -> Text(
                    text = "Error: ${authState.message}",
                    color = Color.Red
                )
                is AuthResult.Loading -> Spacer(Modifier)
                else -> Text(
                    text = "An unknown error has occurred.",
                    color = Color.Red
                )
            }

            Row (
                modifier = Modifier.padding(top = 16.dp)
            )
            {
                Button(
                    onClick = {
                        onLogin(email, password)
                    },
                    modifier.padding(end = 16.dp)

                ) {
                    Text("Log In")
                }

                Button(
                    modifier = Modifier,
                    onClick = onNavigateToRegister,
                ) {
                    Text("Sign Up")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CS446Theme {
        LoginBox(
            onNavigateToRegister = {},
            onLogin = { _, _ -> },
            authState = AuthResult.Loading,
            modifier = Modifier
        )
    }
}
