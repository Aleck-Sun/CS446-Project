package com.example.cs446

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicSecureTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LoginScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreen() {
    CS446Theme {
        Greeting("World")
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
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
                modifier = Modifier
            )
            OutlinedTextField(
                value = "Enter your email",
                onValueChange = {email=it},
                label = { Text("Email")}
            )
            OutlinedTextField(
                value = "",
                onValueChange = {password=it},
                label = { Text("Password")},
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}


@Composable
fun GreetingPreview() {
    CS446Theme {
        Greeting("World")
    }
}