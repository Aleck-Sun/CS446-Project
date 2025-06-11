package com.example.cs446.ui.pages.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cs446.ui.pages.main.MainActivity
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
