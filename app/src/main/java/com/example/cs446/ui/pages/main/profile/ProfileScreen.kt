package com.example.cs446.ui.pages.main.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.backend.data.model.post.Post
import com.example.cs446.common.security.SecurityComponent
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.view.social.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel : ProfileViewModel,
    onNavigate: (MainActivityDestination, String?) -> Unit,
    onLogout: () -> Unit = {}
) {
    val avatar by viewModel.avatar.collectAsState()
    val username by viewModel.username.collectAsState()
    val bio by viewModel.bio.collectAsState()
    var showEditProfile by remember { mutableStateOf(false) }

    val posts by viewModel.posts.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val securityComponent = SecurityComponent()

    fun onSaveProfile(
        avatar: Uri?,
        username: String,
        bio: String
    ): Unit {
        viewModel.updateProfile(
            avatar,
            username,
            bio
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(avatar),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(username, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(bio, fontSize = 14.sp, color = Color.Gray)
            }
        }

        Button(
            onClick = {showEditProfile = true}
        ) {
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileStat("Posts", "120")
            ProfileStat("Followers", "2.5K")
            ProfileStat("Following", "300")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* follow/edit profile */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Follow")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // logout button for testing
        Button(
            onClick = {
                coroutineScope.launch {
                    val success = securityComponent.logoutUser()
                    if (success) {
                        onLogout()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Logout", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        PostsItem(posts = posts)

        if (showEditProfile) {
            CreateEditProfile(
                onDismiss = { showEditProfile = false },
                onSave = ::onSaveProfile,
                usernameDefault = username,
                bioDefault = bio,
                avatarDefault = avatar
            )
        }
    }
}

@Composable
fun ProfileStat(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray)
    }
}

@Composable
fun PostsItem(
    posts : List<Post>
) {
    Text("Posts", fontWeight = FontWeight.Bold)

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(posts) { index, post ->
            if (post.imageUrls.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrls[0]),
                    contentDescription = "Post $index",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Text("No image", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}