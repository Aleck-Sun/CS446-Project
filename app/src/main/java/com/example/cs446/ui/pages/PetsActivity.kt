package com.example.cs446.ui.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.datetime.Instant
import java.util.UUID

import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.data.model.Pet


class PetsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CS446Theme {
                PetProfileScreen()
            }
        }
    }
}

@Composable
fun PetProfileScreen() {
    val pets = listOf(
        Pet(
            id = 1,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            name = "Charlie",
            species = 1,
            breed = "Golden Retriever",
            creatorId = UUID.randomUUID(),
            birthdate = Instant.parse("2025-05-28T00:00:00Z"),
            weight = 65.0
        ),
        Pet(
            id = 2,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            name = "Colin",
            species = 1,
            breed = "Beagle",
            creatorId = UUID.randomUUID(),
            birthdate = Instant.parse("2024-01-15T00:00:00Z"),
            weight = 40.0
        ),
        Pet(
            id = 3,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            name = "Robin",
            species = 1,
            breed = "Poodle",
            creatorId = UUID.randomUUID(),
            birthdate = Instant.parse("2023-11-02T00:00:00Z"),
            weight = 30.0
        )
    )

    var selectedPetIndex by remember { mutableIntStateOf(0) }
    val selectedPet = pets[selectedPetIndex]

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Face, contentDescription = "Pets") },
                    label = { Text("Pets") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO: handle go to Feed page */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {  /* TODO: handle go to Profile page */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Top icons (pets + add pet)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                pets.forEachIndexed { index, pet ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedPetIndex = index
                        }
                    ) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = pet.name,
                            modifier = Modifier.size(56.dp),
                            tint = if (index == selectedPetIndex) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { /* TODO: handle add pet */ }
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Pet", modifier = Modifier.size(56.dp))
                    Text("Add Pet", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pet info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(selectedPet.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    val birthdateString = selectedPet.birthdate.toString()

                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Breed", fontWeight = FontWeight.Medium)
                            Text(selectedPet.breed ?: "Unknown")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date of Birth", fontWeight = FontWeight.Medium)
                            Text(birthdateString)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sex", fontWeight = FontWeight.Medium)
                            // You don't have sex in your Pet class. You might add it or leave blank
                            Text("Unknown")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weight", fontWeight = FontWeight.Medium)
                            Text("${selectedPet.weight} lbs")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logs button with unread notifications
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { /* handle logs click */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logs")
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .background(Color.Red, shape = CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("8", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Family button
            Button(
                onClick = { /* handle family click */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Family")
            }
        }
    }
}
