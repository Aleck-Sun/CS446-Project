package com.example.cs446.ui.pages.main.pets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cs446.ui.pages.main.MainActivityDestination

@Composable
fun FamilyScreen(
    modifier: Modifier = Modifier,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
