package com.example.cs446.ui.pages.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cs446.ui.pages.login.LoginActivity
import com.example.cs446.view.pets.PetsViewModel
import com.example.cs446.view.social.FeedViewModel
import com.example.cs446.view.pets.HandlerViewModel
import com.example.cs446.view.social.ProfileViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val petsViewModel = PetsViewModel()
        val feedViewModel = FeedViewModel()
        val profileViewModel = ProfileViewModel()
        val handlerViewModel = HandlerViewModel()

        val onLogout = {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setContent {
            MainNavigator(
                petsViewModel = petsViewModel,
                feedViewModel = feedViewModel,
                profileViewModel = profileViewModel,
                onLogout = onLogout,
                handlerViewModel = handlerViewModel
            )
        }
    }
}
