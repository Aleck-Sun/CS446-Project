package com.example.cs446.ui.pages

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.ui.components.BottomNavigation

class FamilyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CS446Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigation(currentScreen = "pets") { screen ->
                            when (screen) {
                                "pets" -> {
                                    startActivity(Intent(this, PetsActivity::class.java))
                                    finish()
                                }
                                "feed" -> {
                                    startActivity(Intent(this, FeedActivity::class.java))
                                    finish()
                                }
                                "profile" -> {
                                    startActivity(Intent(this, ProfileActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    FamilyContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun FamilyContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Family Page",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Hello Family Page!",
                fontSize = 18.sp
            )
        }
    }
}
