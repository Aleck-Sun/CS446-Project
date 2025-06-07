package com.example.cs446.ui.pages.login

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
import com.example.cs446.backend.data.result.AuthResult
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.view.security.SecurityViewModel

@Composable
fun RegisterScreen(
    viewModel: SecurityViewModel = SecurityViewModel(),
    onNavigateToLogin: () -> Unit = {},
    onRegistered: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthResult.RegisterSuccess) {
            onRegistered()
        }
    }
    CS446Theme {
        RegisterBox(
            onNavigateToLogin = onNavigateToLogin,
            onRegister = { email: String, password: String ->
                viewModel.signUp(email, password)
            },
            authState = authState,
            modifier = modifier
        )
    }
}

@Composable
fun RegisterBox(
    onNavigateToLogin: () -> Unit,
    onRegister: (String, String) -> Unit,
    authState: AuthResult,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Let's Get Started",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Please enter an email and password",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.padding(bottom = 8.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(bottom = 8.dp)

            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(bottom = 8.dp)

            )

            when (authState) {
                is AuthResult.RegisterSuccess -> Text(
                    text = "Login successful",
                    color = Color.Green
                )
                is AuthResult.RegisterError -> Text(
                    text = "Error: ${authState.message}",
                    color = Color.Red
                )
                is AuthResult.Loading -> Spacer(Modifier)
                else -> Text(
                    text = "An unknown error has occurred.",
                    color = Color.Red
                )
            }

            Row(
                modifier = Modifier.padding(top = 16.dp)
            )
            {
                Button(
                    modifier = Modifier,
                    onClick = {
                        onRegister(email, password)
                    },
                ) {
                    Text("Register")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    CS446Theme {
        RegisterBox(
            onNavigateToLogin = { },
            onRegister = { _, _ -> },
            authState = AuthResult.Loading,
            modifier = Modifier
        )
    }
}
