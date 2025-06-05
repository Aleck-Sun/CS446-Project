package com.example.cs446.ui.pages

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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.cs446.SupabaseClient
import com.example.cs446.ui.theme.CS446Theme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fun onLogIn() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        fun onSignUpRequest() {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        setContent {
            LoginScreen(
                ::onLogIn,
                ::onSignUpRequest
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreen(
    onLogIn: () -> Unit = {},
    onSignUpRequest: () -> Unit = {}
) {
    CS446Theme {
        LoginBox(
            onLogIn,
            onSignUpRequest
        )
    }
}


@Composable
fun LoginBox(
    onLogIn: () -> Unit,
    onSignUpRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var incorrectLogin by remember { mutableStateOf(false) }

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
                label = { Text("Email")},
                modifier = Modifier.padding(bottom = 8.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = {password=it},
                label = { Text("Password")},
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(bottom = 8.dp)

            )
            if (incorrectLogin) {
                Text(
                    text = "Incorrect Username or Password",
                    color = Color.Red,
                )
            }

            Row (
                modifier = Modifier.padding(top = 16.dp)
            )
            {
                Button(
                    onClick = {
                        incorrectLogin = false
                        SupabaseClient.loginWithSupabase(
                            email = email,
                            password = password,
                            onSuccess = onLogIn,
                            onError = {
                                incorrectLogin = true
                            }
                        )
                    },
                    modifier.padding(end = 16.dp)

                ) {
                    Text("Log In")
                }

                Button(
                    modifier = Modifier,
                    onClick = onSignUpRequest,
                ) {
                    Text("Sign Up")
                }
            }
        }
    }
}
